package com.example.practicaTransacciones.util;

import com.example.practicaTransacciones.domain.Transaccion;
import com.example.practicaTransacciones.repository.CuentaRepository;
import com.example.practicaTransacciones.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudeScoreCalculator {


    private static final BigDecimal UMBRAL_MONTO    = new BigDecimal("10000");//Evita errores de representacion coma
    private static final double PESO_MONTO          = 0.30;
    private static final double PESO_HORARIO        = 0.20;
    private static final double PESO_FRECUENCIA     = 0.25;
    private static final double PESO_CUENTA_NUEVA   = 0.15;
    private static final double PESO_PAIS           = 0.10;

    private final TransaccionRepository transaccionRepository;
    private final CuentaRepository cuentaRepository;

    public double calcularScore(Transaccion transaccion) {
        double score = 0.0;

        //Monto > 10.000€ -> peso 0.30
        if (transaccion.getMonto().compareTo(UMBRAL_MONTO) > 0) {
            score += PESO_MONTO;
            log.debug("Indicador monto alto activado: {}", transaccion.getMonto());
        }

        //Hora entre las 0 y las 5 mañana -> peso 0.20
        int hora = transaccion.getFechaHora().getHour();
        if (hora < 5) {
            score += PESO_HORARIO;
            log.debug("Indicador horario nocturno activado: hora {}", hora);
        }

        // Más de 3 transacciones en últimos 5 min -> peso 0.25
        LocalDateTime cincoMinutosAtras = transaccion.getFechaHora().minusMinutes(5);
        long transaccionesRecientes = transaccionRepository.countTransaccionesRecientes(
                transaccion.getCuentaOrigen(), cincoMinutosAtras);
        if (transaccionesRecientes > 3) {
            score += PESO_FRECUENCIA;
            log.debug("Indicador frecuencia alta activado: {} transacciones", transaccionesRecientes);
        }

        // Cuenta destino creada hace < 7 días -> peso 0.15
        if (esDestinoCuentaNueva(transaccion.getCuentaDestino())) {
            score += PESO_CUENTA_NUEVA;
            log.debug("Indicador cuenta destino nueva activado");
        }

        // País destino distinto al habitual -> peso 0.10
        if (esPaisDistintoAlHabitual(transaccion.getCuentaOrigen(), transaccion.getCuentaDestino())) {
            score += PESO_PAIS;
            log.debug("Indicador país distinto al habitual activado");
        }

        log.warn("Score de fraude calculado: {} para transacción origen: {}",
                score, transaccion.getCuentaOrigen());
        return score;
    }

    private boolean esDestinoCuentaNueva(String cuentaDestino) {
        // Consulta la fecha de alta real de la cuenta en la BD
        // Si fue creada hace menos de 7 días -> cuenta nueva -> devuelve true
        return cuentaRepository.findByNumeroCuenta(cuentaDestino)
                .map(cuenta -> cuenta.getFechaAlta()
                        .isAfter(LocalDate.now().minusDays(7)))
                .orElse(false);
    }

    private boolean esPaisDistintoAlHabitual(String cuentaOrigen, String cuentaDestino) {
        //Cuenta nula o con menos de 2 caracteres no activa indicador pais
        //ES, FR, EN
        if (cuentaOrigen == null || cuentaDestino == null
                || cuentaOrigen.length() < 2 || cuentaDestino.length() < 2) {
            return false;
        }

        // Obtiene todas las transacciones de la cuenta origen
        // y busca el país más frecuente en sus destinos históricos
        String paisHabitual = transaccionRepository.findByCuentaOrigen(cuentaOrigen)
                .stream()
                .filter(t -> t.getCuentaDestino() != null && t.getCuentaDestino().length() >= 2)
                .map(t -> t.getCuentaDestino().substring(0, 2))  // Extrae código de país del IBAN
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()))//Agrupa el código
                .entrySet().stream()//Busca la entrada con mayor valor para saber pais habitual
                .max(Map.Entry.comparingByValue())  //Devuelve el codigo de pais
                .map(Map.Entry::getKey)
                .orElse(cuentaOrigen.substring(0, 2)); // Si no hay historial usa el país de origen

        String paisDestino = cuentaDestino.substring(0, 2);

        return !paisHabitual.equals(paisDestino);
        // true -> país distinto al habitual -> activa el indicador ( +0.10)
    }
}
