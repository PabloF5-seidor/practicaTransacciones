package com.example.practicaTransacciones.service;

import com.example.practicaTransacciones.dto.CuentaDTOResponse;

public interface CuentaService {
    CuentaDTOResponse resumenCuenta(Long cuentaId);
}
