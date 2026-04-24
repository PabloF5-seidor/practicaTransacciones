package com.example.practicaTransacciones.service;

import com.example.practicaTransacciones.dto.AlertaFraudeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AlertaFraudeService {

    Page<AlertaFraudeResponseDTO> obtenerAlertasNoRevisadas(Pageable pageable);

    AlertaFraudeResponseDTO revisarAlerta(Long id);
}
