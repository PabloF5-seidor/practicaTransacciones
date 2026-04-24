package com.example.practicaTransacciones.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RankingCuentaResponseDTO(
        Long cuentaId,
        String numeroCuenta,
        BigDecimal saldoActual,
        Double scoreRiesgoAcumulado,
        long alertasCriticas,
        LocalDate fechaAlta
) {
}
