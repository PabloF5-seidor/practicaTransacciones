package com.example.practicaTransacciones.service;

import com.example.practicaTransacciones.dto.CuentaDTOResponse;
import com.example.practicaTransacciones.dto.RankingCuentaResponseDTO;

import java.util.List;

public interface CuentaService {
    CuentaDTOResponse resumenCuenta(Long cuentaId);
    List<RankingCuentaResponseDTO> obtenerRankingRiesgo();
}
