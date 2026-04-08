package com.example.practicaTransacciones.controller;

import com.example.practicaTransacciones.dto.TransaccionDTOResponse;
import com.example.practicaTransacciones.service.TransaccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class TransaccionController {
    private final TransaccionService transaccionService;

    @GetMapping("/{id}/estado")
    public ResponseEntity<TransaccionDTOResponse> obtenerEstado(@PathVariable Long id) {
        return ResponseEntity.ok(transaccionService.obtenerEstado(id));
    }
}
