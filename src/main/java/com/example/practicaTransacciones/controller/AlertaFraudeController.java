package com.example.practicaTransacciones.controller;

import com.example.practicaTransacciones.dto.AlertaFraudeResponseDTO;
import com.example.practicaTransacciones.service.AlertaFraudeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fraude")
@RequiredArgsConstructor
@Tag(name = "Fraude", description = "Gestión de alertas de fraude")
public class AlertaFraudeController {

    private final AlertaFraudeService alertaFraudeService;

    @GetMapping("/alertas")
    @Operation(summary = "Obtener alertas no revisadas ordenadas por nivel y fecha")
    public ResponseEntity<Page<AlertaFraudeResponseDTO>> obtenerAlertas(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(alertaFraudeService.obtenerAlertasNoRevisadas(pageable));
    }

    @PutMapping("/alertas/{id}/revisar")
    @Operation(summary = "Marcar una alerta como revisada. Si es CRITICA dispara notificación asíncrona")
    public ResponseEntity<AlertaFraudeResponseDTO> revisarAlerta(@PathVariable Long id) {
        return ResponseEntity.ok(alertaFraudeService.revisarAlerta(id));
    }
}