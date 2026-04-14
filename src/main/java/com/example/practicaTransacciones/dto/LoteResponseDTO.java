package com.example.practicaTransacciones.dto;

import java.util.List;

public record LoteResponseDTO (
        int totalRecibidas,
        int procesadasOk,
        int procesadasError,
        List<String> errores
){}
