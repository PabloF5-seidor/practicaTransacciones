package com.example.practicaTransacciones.dto;

public record TransferenciaValidaResponse (
        Long idSeguimiento,   // ID transacción para consultar con GET /estado
        String estado,
        String mensaje
){}
