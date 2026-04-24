package com.example.practicaTransacciones.exception;

public class AlertaFraudeNotFoundException extends RuntimeException {
    public AlertaFraudeNotFoundException(Long id) {
        super("Alerta Fraude no encontrada con id: " + id);
    }
}
