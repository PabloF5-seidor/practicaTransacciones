package com.example.practicaTransacciones.exception;

public class TransaccionNotFoundException extends RuntimeException {
    public TransaccionNotFoundException(Long id) {
        super("Transacción no encontrada con ID: " + id);
    }
}
