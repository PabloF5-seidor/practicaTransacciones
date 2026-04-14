package com.example.practicaTransacciones.service;

import com.example.practicaTransacciones.dto.TransaccionDTOResponse;
import com.example.practicaTransacciones.dto.TransferenciaRequest;
import com.example.practicaTransacciones.dto.TransferenciaValidaResponse;


public interface TransaccionService {
    TransaccionDTOResponse obtenerEstado(Long id);
    TransferenciaValidaResponse procesarTransferencia(TransferenciaRequest request);
}

