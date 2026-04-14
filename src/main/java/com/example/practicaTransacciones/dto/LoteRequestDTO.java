package com.example.practicaTransacciones.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record LoteRequestDTO (
        @NotEmpty(message = "El lote no puede estar vacío")
        @Size(max = 500, message = "El lote no puede superar 500 transacciones")
        @Valid
        List<TransferenciaRequest> transacciones

){}


