package com.example.practicaTransacciones.dto;

import com.example.practicaTransacciones.domain.NivelRiesgo;

import java.time.LocalDateTime;

public record AlertaFraudeResponseDTO(
        Long id,
        Long transaccionId,
        NivelRiesgo nivel,
        String motivo,
        Boolean revisada,
        LocalDateTime fechaHora
){}

