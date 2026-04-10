package com.example.practicaTransacciones.controller;

import com.example.practicaTransacciones.dto.CuentaDTOResponse;
import com.example.practicaTransacciones.service.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
@Tag(name = "Cuentas")
public class CuentaController {
    private final CuentaService cuentaService;
    @Operation(summary="Obtener resumen de cuenta")
    @GetMapping("/{id}/resumenCuenta")
    public ResponseEntity<CuentaDTOResponse> getCuenta(@PathVariable Long id){
        return ResponseEntity.ok(cuentaService.resumenCuenta(id));
    }
}
