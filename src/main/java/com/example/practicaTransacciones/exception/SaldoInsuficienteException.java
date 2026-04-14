package com.example.practicaTransacciones.exception;

public class SaldoInsuficienteException extends RuntimeException {
    public SaldoInsuficienteException(String numeroCuenta) {
        super("La cuenta " + numeroCuenta + " no tiene saldo suficiente para la operación");
    }
}
