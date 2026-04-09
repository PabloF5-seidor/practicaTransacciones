package com.example.practicaTransacciones.dto;

import com.example.practicaTransacciones.domain.EstadoTransaccion;
import com.example.practicaTransacciones.domain.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record TransaccionDTOResponse(
        Long id,
        String cuentaOrigen,
        String cuentaDestino,
        BigDecimal monto,
        TipoTransaccion tipo,
        EstadoTransaccion estado,
      LocalDateTime fechaHora,
        Double riesgoFraude
) {}