package com.example.practicaTransacciones.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
@RestControllerAdvice //global intercepta toda excepcion
public class GlobalExceptionHandler {

    private Map<String, Object> buildError(HttpStatus status, String error,
                                           String detalle, String path) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", error,
                "detalle", detalle,
                "path", path
        );
    }
    //Error 400
    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleSaldoInsuficiente(
            SaldoInsuficienteException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, "Saldo insuficiente",
                        ex.getMessage(), request.getRequestURI()));
    }
    //Error 403
    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaBloqueada(
            CuentaBloqueadaException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(HttpStatus.FORBIDDEN, "Cuenta bloqueada",
                        ex.getMessage(), request.getRequestURI()));
    }
    //Error 404
    @ExceptionHandler(TransaccionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTransaccionNotFound(
            TransaccionNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, "Transacción no encontrada",
                        ex.getMessage(), request.getRequestURI()));
    }
    @ExceptionHandler(AlertaFraudeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAlertaNotFound(
            AlertaFraudeNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, "Alerta no encontrada",
                        ex.getMessage(), request.getRequestURI()));
    }
    //Error 409
    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<Map<String, Object>> handleConcurrency(
            ConcurrencyFailureException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, "Conflicto de concurrencia",
                        ex.getMessage(), request.getRequestURI()));
    }
    //Error 404
    @ExceptionHandler(CuentaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaNotFound(
            CuentaNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, "Cuenta no encontrada",
                        ex.getMessage(), request.getRequestURI()));
    }
    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                        ex.getMessage(), request.getRequestURI()));
    }
}