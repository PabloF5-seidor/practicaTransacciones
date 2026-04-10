package com.example.practicaTransacciones.exception;

public class CuentaNotFoundException extends RuntimeException {
    public CuentaNotFoundException(Long id) {
        super("Cuenta no encontrada con id: " + id);
    }
}
