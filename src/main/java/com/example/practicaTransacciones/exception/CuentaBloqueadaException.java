package com.example.practicaTransacciones.exception;

public class CuentaBloqueadaException extends RuntimeException {
    public CuentaBloqueadaException(String numeroCuenta) {
        super("La cuenta " + numeroCuenta + " está bloqueada y no puede operar");
    }
}
