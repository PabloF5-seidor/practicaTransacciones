package com.example.practicaTransacciones.service;

import com.example.practicaTransacciones.dto.*;


public interface TransaccionService {
    TransaccionDTOResponse obtenerEstado(Long id);
    TransferenciaValidaResponse procesarTransferencia(TransferenciaRequest request);
    LoteResponseDTO procesarLote(LoteRequestDTO loteRequestDTO);
}

