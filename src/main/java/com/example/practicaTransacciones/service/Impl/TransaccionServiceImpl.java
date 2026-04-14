package com.example.practicaTransacciones.service.Impl;

import com.example.practicaTransacciones.domain.Cuenta;
import com.example.practicaTransacciones.domain.EstadoTransaccion;
import com.example.practicaTransacciones.domain.TipoTransaccion;
import com.example.practicaTransacciones.domain.Transaccion;
import com.example.practicaTransacciones.dto.TransaccionDTOResponse;
import com.example.practicaTransacciones.dto.TransferenciaRequest;
import com.example.practicaTransacciones.dto.TransferenciaValidaResponse;
import com.example.practicaTransacciones.exception.CuentaBloqueadaException;
import com.example.practicaTransacciones.exception.SaldoInsuficienteException;
import com.example.practicaTransacciones.exception.TransaccionNotFoundException;
import com.example.practicaTransacciones.mapper.TransaccionMapper;
import com.example.practicaTransacciones.repository.CuentaRepository;
import com.example.practicaTransacciones.repository.TransaccionRepository;
import com.example.practicaTransacciones.service.TransaccionService;
import com.example.practicaTransacciones.util.FraudeScoreCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransaccionServiceImpl implements TransaccionService {
    private final TransaccionRepository transaccionRepository;
    private final TransaccionMapper transaccionMapper;
    private final CuentaRepository cuentaRepository;
    private final FraudeScoreCalculator fraudeScoreCalculator;


    @Transactional(readOnly = true)
    public TransaccionDTOResponse obtenerEstado(Long id) {
        //UUID -> Valores de 128 bits que son ids únicos globalmente
        //MDC -> Añade datos a los logs generados en una ejecución
        MDC.put("correlationId", UUID.randomUUID().toString());

        //Buca en bbdd transacciones con id ( devuelve Optional)
        var transaccion = transaccionRepository.findById(id)
                //Si está vacío lanza exception (404)
                .orElseThrow(() -> new TransaccionNotFoundException(id));

        log.info("Transacción con id:" + id);
        MDC.clear();
        return transaccionMapper.toEstadoResponse(transaccion);
    }

    @Transactional
    public TransferenciaValidaResponse procesarTransferencia(TransferenciaRequest request) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Iniciando transferencia de {} a {}", request.cuentaOrigen(), request.cuentaDestino());

        // Obtener cuenta origen con bloqueo pesimista para evitar condiciones de carrera
        Cuenta origen = cuentaRepository.findByNumeroCuentaWithLock(request.cuentaOrigen())
                .orElseThrow(() -> new CuentaBloqueadaException(request.cuentaOrigen()));

        // Validar que la cuenta no está bloqueada ni cerrada
        switch (origen.getEstado()) {
            case BLOQUEADA -> throw new CuentaBloqueadaException(request.cuentaOrigen());
            case CERRADA   -> throw new CuentaBloqueadaException(request.cuentaOrigen());
            default        -> log.debug("Cuenta origen en estado válido: {}", origen.getEstado());
        }

        // Validar saldo suficiente
        if (origen.getSaldo().compareTo(request.monto()) < 0) {
            throw new SaldoInsuficienteException(request.cuentaOrigen());
        }

        //  Crear la transacción en estado PENDIENTE y persistirla
        Transaccion transaccion = Transaccion.builder()
                .cuentaOrigen(request.cuentaOrigen())
                .cuentaDestino(request.cuentaDestino())
                .monto(request.monto())
                .tipo(TipoTransaccion.TRANSFERENCIA)
                .estado(EstadoTransaccion.PENDIENTE)
                .fechaHora(LocalDateTime.now())
                .descripcion(request.descripcion())
                .cuentaOrigenRef(origen)
                .build();

        transaccion = transaccionRepository.save(transaccion);
        Long transaccionId = transaccion.getId();
        log.info("Transacción persistida con id: {} en estado PENDIENTE", transaccionId);

        //Lanzar el procesamiento asíncrono (hilo separado)
        procesarTransferenciaAsync(transaccionId, request);

        MDC.clear();

        //Devuelve 202 Accepted inmediatamente con el ID de seguimiento
        return new TransferenciaValidaResponse(
                transaccionId,
                EstadoTransaccion.PENDIENTE.name(),
                "Transferencia en proceso. Use el ID de seguimiento para consultar el estado."
        );
    }

    @Async("transaccionExecutor") // Se ejecuta en un hilo del pool configurado en Async
    @Transactional
    public void procesarTransferenciaAsync(Long transaccionId, TransferenciaRequest request) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Procesando transferencia asíncrona id: {}", transaccionId);

        try {
            Transaccion transaccion = transaccionRepository.findById(transaccionId)
                    .orElseThrow(() -> new TransaccionNotFoundException(transaccionId));

            // Cambiar estado a PROCESANDO
            transaccion.setEstado(EstadoTransaccion.PROCESANDO);
            transaccionRepository.save(transaccion);

            // Calcular score de fraude en paralelo con la persistencia
            double scoreFragude = fraudeScoreCalculator.calcularScore(transaccion);
            transaccion.setRiesgoFraude(scoreFragude);

            // Si el score supera 0.75 bloquear automáticamente
            if (scoreFragude > 0.75) {
                log.warn("Riesgo de fraude alto ({}) en transacción id: {}", scoreFragude, transaccionId);
                transaccion.setEstado(EstadoTransaccion.RECHAZADA);
                transaccionRepository.save(transaccion);
                return;
            }

            // Descontar saldo de la cuenta origen
            Cuenta origen = cuentaRepository.findByNumeroCuentaWithLock(request.cuentaOrigen())
                    .orElseThrow(() -> new CuentaBloqueadaException(request.cuentaOrigen()));
            origen.setSaldo(origen.getSaldo().subtract(request.monto()));
            cuentaRepository.save(origen);//guardar

            // Marcar como completada
            transaccion.setEstado(EstadoTransaccion.COMPLETADA);
            transaccionRepository.save(transaccion);

            log.info("Transferencia id: {} completada con éxito. Score fraude: {}", transaccionId, scoreFragude);

        } catch (Exception e) {
            log.error("Error procesando transferencia id: {}. Causa: {}", transaccionId, e.getMessage());
            transaccionRepository.findById(transaccionId).ifPresent(t -> {
                t.setEstado(EstadoTransaccion.RECHAZADA);
                transaccionRepository.save(t);
            });
        } finally { // Para que siempre termine MDC.clear() sin importar resultado
            MDC.clear();
        }
    }
}

