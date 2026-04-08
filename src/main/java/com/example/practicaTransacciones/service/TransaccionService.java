package com.example.practicaTransacciones.service;

import com.example.practicaTransacciones.dto.TransaccionDTOResponse;


public interface TransaccionService {
    TransaccionDTOResponse obtenerEstado(Long id);
}
