package com.example.practicaTransacciones.controller;

import com.example.practicaTransacciones.dto.TransaccionDTOResponse;
import com.example.practicaTransacciones.dto.TransferenciaRequest;
import com.example.practicaTransacciones.dto.TransferenciaValidaResponse;
import com.example.practicaTransacciones.service.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class TransaccionController {
    private final TransaccionService transaccionService;
    @GetMapping("/{id}/estado")
    public ResponseEntity<TransaccionDTOResponse> obtenerEstado(@PathVariable Long id) {
        return ResponseEntity.ok(transaccionService.obtenerEstado(id));
    }
    @PostMapping("/transferencia")
    @Operation(summary = "Procesar una transferencia entre dos cuentas")
    public ResponseEntity<TransferenciaValidaResponse> procesarTransferencia(
            @Valid @RequestBody TransferenciaRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(transaccionService.procesarTransferencia(request));
    }
}
