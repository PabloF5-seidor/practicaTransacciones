package com.example.practicaTransacciones.service.Impl;

import com.example.practicaTransacciones.domain.Cuenta;
import com.example.practicaTransacciones.domain.EstadoTransaccion;
import com.example.practicaTransacciones.domain.TipoTransaccion;
import com.example.practicaTransacciones.domain.Transaccion;
import com.example.practicaTransacciones.dto.*;
import com.example.practicaTransacciones.exception.CuentaBloqueadaException;
import com.example.practicaTransacciones.exception.SaldoInsuficienteException;
import com.example.practicaTransacciones.exception.TransaccionNotFoundException;
import com.example.practicaTransacciones.mapper.TransaccionMapper;
import com.example.practicaTransacciones.repository.CuentaRepository;
import com.example.practicaTransacciones.repository.TransaccionRepository;
import com.example.practicaTransacciones.service.TransaccionService;
import com.example.practicaTransacciones.util.FraudeScoreCalculator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class TransaccionServiceImpl implements TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final TransaccionMapper transaccionMapper;
    private final CuentaRepository cuentaRepository;
    private final FraudeScoreCalculator fraudeScoreCalculator;
    private final Executor transaccionExecutor;

    @Autowired
    public TransaccionServiceImpl(
            TransaccionRepository transaccionRepository,
            TransaccionMapper transaccionMapper,
            CuentaRepository cuentaRepository,
            FraudeScoreCalculator fraudeScoreCalculator,
            @Qualifier("transaccionExecutor") Executor transaccionExecutor) {//Lombok no es compatible con Qualifier
        this.transaccionRepository = transaccionRepository;
        this.transaccionMapper = transaccionMapper;
        this.cuentaRepository = cuentaRepository;
        this.fraudeScoreCalculator = fraudeScoreCalculator;
        this.transaccionExecutor = transaccionExecutor;
    }

   //Endpoint 2
    @Transactional(readOnly = true)
    @Override
    public TransaccionDTOResponse obtenerEstado(Long id) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Consultando estado de transacción con id: {}", id);

        var transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new TransaccionNotFoundException(id));

        log.info("Transacción encontrada con id: {}", id);
        MDC.clear();
        return transaccionMapper.toEstadoResponse(transaccion);
    }
    //Endpoint 1
    @Transactional
    @Override
    public TransferenciaValidaResponse procesarTransferencia(TransferenciaRequest request) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Iniciando transferencia de {} a {}", request.cuentaOrigen(), request.cuentaDestino());

        // Bloqueo pesimista: bloquea la fila hasta que termine la transacción
        // Evita que dos transferencias simultáneas corrompan el saldo
        Cuenta origen = cuentaRepository.findByNumeroCuentaWithLock(request.cuentaOrigen())
                .orElseThrow(() -> new CuentaBloqueadaException(request.cuentaOrigen()));

        // Validar estado de la cuenta
        switch (origen.getEstado()) {
            case BLOQUEADA -> throw new CuentaBloqueadaException(request.cuentaOrigen());
            case CERRADA   -> throw new CuentaBloqueadaException(request.cuentaOrigen());
            default        -> log.debug("Cuenta origen en estado válido: {}", origen.getEstado());
        }

        // Validar saldo suficiente
        if (origen.getSaldo().compareTo(request.monto()) < 0) {
            throw new SaldoInsuficienteException(request.cuentaOrigen());
        }

        // Crear transacción en estado PENDIENTE y persistir
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

        // Lanzar procesamiento asíncrono en hilo separado
        procesarTransferenciaAsync(transaccionId, request);

        MDC.clear();

        // Devuelve 200
        return new TransferenciaValidaResponse(
                transaccionId,
                EstadoTransaccion.PENDIENTE.name(),
                "Transferencia en proceso. Use el ID de seguimiento para consultar el estado."
        );
    }

    @Async("transaccionExecutor") //Hilo de async
    @Transactional
    public void procesarTransferenciaAsync(Long transaccionId, TransferenciaRequest request) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Procesando transferencia asíncrona id: {}", transaccionId);

        try {
            Transaccion transaccion = transaccionRepository.findById(transaccionId)
                    .orElseThrow(() -> new TransaccionNotFoundException(transaccionId));

            // Cambiar a PROCESANDO
            transaccion.setEstado(EstadoTransaccion.PROCESANDO);
            transaccionRepository.save(transaccion);

            // Calcular score de fraude
            double scoreFraude = fraudeScoreCalculator.calcularScore(transaccion);
            transaccion.setRiesgoFraude(scoreFraude);

            // Si supera 0.75 -> rechazar automáticamente
            if (scoreFraude > 0.75) {
                log.warn("Riesgo de fraude alto ({}) en transacción id: {}", scoreFraude, transaccionId);
                transaccion.setEstado(EstadoTransaccion.RECHAZADA);
                transaccionRepository.save(transaccion);
                return;
            }

            // Descontar saldo
            Cuenta origen = cuentaRepository.findByNumeroCuentaWithLock(request.cuentaOrigen())
                    .orElseThrow(() -> new CuentaBloqueadaException(request.cuentaOrigen()));
            origen.setSaldo(origen.getSaldo().subtract(request.monto()));
            cuentaRepository.save(origen);

            // Marcar como completada
            transaccion.setEstado(EstadoTransaccion.COMPLETADA);
            transaccionRepository.save(transaccion);

            log.info("Transferencia id: {} completada. Score fraude: {}", transaccionId, scoreFraude);

        } catch (Exception e) {
            log.error("Error procesando transferencia id: {}. Causa: {}", transaccionId, e.getMessage());
            transaccionRepository.findById(transaccionId).ifPresent(t -> {
                t.setEstado(EstadoTransaccion.RECHAZADA);
                transaccionRepository.save(t);
            });
        } finally {
            MDC.clear();
        }
    }

    //Endpoint 3
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public LoteResponseDTO procesarLote(LoteRequestDTO loteRequest) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Iniciando procesamiento de lote con {} transacciones",
                loteRequest.transacciones().size());

        AtomicInteger procesadasOk    = new AtomicInteger(0);
        AtomicInteger procesadasError = new AtomicInteger(0);
        List<String> errores = Collections.synchronizedList(new ArrayList<>());

        // Partir el lote en sublotes de 50
        List<List<TransferenciaRequest>> sublotes = partirEnSublotes(
                loteRequest.transacciones(), 50);

        log.info("Lote dividido en {} sublotes de 50", sublotes.size());

        // Procesar cada sublote en un hilo separado del pool
        List<CompletableFuture<Void>> futuros = sublotes.stream()
                .map(sublote -> CompletableFuture.runAsync(
                        () -> procesarSublote(sublote, procesadasOk, procesadasError, errores),
                        transaccionExecutor
                ))
                .toList();

        // Esperar a que todos los hilos terminen
        CompletableFuture.allOf(futuros.toArray(new CompletableFuture[0])).join();

        log.info("Lote completado. OK: {} | Error: {}", procesadasOk.get(), procesadasError.get());
        MDC.clear();

        return new LoteResponseDTO(
                loteRequest.transacciones().size(),
                procesadasOk.get(),
                procesadasError.get(),
                errores
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void procesarSublote(List<TransferenciaRequest> sublote,
                                AtomicInteger procesadasOk,
                                AtomicInteger procesadasError,
                                List<String> errores) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Procesando sublote de {} transacciones", sublote.size());

        for (TransferenciaRequest request : sublote) {
            try {
                procesarTransferencia(request);
                procesadasOk.incrementAndGet();
            } catch (Exception e) {
                procesadasError.incrementAndGet();
                String error = "Error en transferencia " + request.cuentaOrigen()
                        + " -> " + request.cuentaDestino() + ": " + e.getMessage();
                errores.add(error);
                log.error(error);
            }
        }
        MDC.clear();
    }

    private <T> List<List<T>> partirEnSublotes(List<T> lista, int tamanio) {
        List<List<T>> sublotes = new ArrayList<>();
        for (int i = 0; i < lista.size(); i += tamanio) {
            sublotes.add(lista.subList(i, Math.min(i + tamanio, lista.size())));
        }
        return sublotes;
    }
}