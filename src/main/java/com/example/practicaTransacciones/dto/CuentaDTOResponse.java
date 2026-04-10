package com.example.practicaTransacciones.dto;

import java.math.BigDecimal;

public record CuentaDTOResponse (
    Long cuentaId,
     String numeroCuenta,
    BigDecimal saldoActual,
    long totalMovimientos,
     BigDecimal montoPromedio,
    BigDecimal desviacionEstandar,
    Double puntuacionRiesgoAcumulada
) {}
