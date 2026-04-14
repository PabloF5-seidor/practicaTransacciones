package com.example.practicaTransacciones.dto;


import jakarta.validation.constraints.*;
import java.math.BigDecimal;


public record TransferenciaRequest (
    @NotBlank(message = "La cuenta origen es obligatoria")
    @Pattern(regexp = "^[A-Z]{2}\\d{2}(-\\d{4}){5}$", message = "Formato IBAN inválido")
    String cuentaOrigen,

    @NotBlank(message = "La cuenta destino es obligatoria")
    @Pattern(regexp = "^[A-Z]{2}\\d{2}(-\\d{4}){5}$", message = "Formato IBAN inválido")
    String cuentaDestino,

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    @DecimalMax(value = "50000.00", message = "El monto no puede superar 50.000€")
    BigDecimal monto,

    String descripcion
){}
