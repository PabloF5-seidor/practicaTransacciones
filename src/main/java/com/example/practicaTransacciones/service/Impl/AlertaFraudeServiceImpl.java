package com.example.practicaTransacciones.service.Impl;

import com.example.practicaTransacciones.domain.AlertaFraude;
import com.example.practicaTransacciones.domain.NivelRiesgo;
import com.example.practicaTransacciones.dto.AlertaFraudeResponseDTO;
import com.example.practicaTransacciones.exception.AlertaFraudeNotFoundException;
import com.example.practicaTransacciones.mapper.AlertaFraudeMapper;
import com.example.practicaTransacciones.repository.AlertaFraudeRepository;
import com.example.practicaTransacciones.service.AlertaFraudeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertaFraudeServiceImpl implements AlertaFraudeService {
    private final AlertaFraudeRepository alertaFraudeRepository;
    private final AlertaFraudeMapper alertaFraudeMapper;

    @Transactional(readOnly = true)
    @Override
    public Page<AlertaFraudeResponseDTO> obtenerAlertasNoRevisadas(Pageable pageable) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Obteniendo alertas no revisadas - página: {}", pageable.getPageNumber());

        Page<AlertaFraudeResponseDTO> resultado = alertaFraudeRepository
                .findByRevisadaFalse(pageable)
                .map(alertaFraudeMapper::toResponse);

        log.info("Alertas no revisadas encontradas: {}", resultado.getTotalElements());
        MDC.clear();
        return resultado;
    }
    @Transactional
    @Override
    public AlertaFraudeResponseDTO revisarAlerta(Long id) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Revisando alerta con id: {}", id);

        // Busca la alerta o lanza 404
        AlertaFraude alerta = alertaFraudeRepository.findById(id)
                .orElseThrow(() -> new AlertaFraudeNotFoundException(id));

        // Marca como revisada
        alerta.setRevisada(true);
        alertaFraudeRepository.save(alerta);

        log.info("Alerta id: {} marcada como revisada. Nivel: {}", id, alerta.getNivel());

        // Si es CRITICO dispara notificación asíncrona
        if (NivelRiesgo.CRITICO.equals(alerta.getNivel())) {
            log.warn("Alerta CRITICA id: {} — disparando notificación asíncrona", id);
            enviarNotificacion(alerta);
        }

        MDC.clear();
        return alertaFraudeMapper.toResponse(alerta);
    }
    @Async("transaccionExecutor")
    // Reutiliza el mismo pool de hilos configurado en AsyncConfig
    // No bloquea la respuesta al cliente mientras se envía la notificación
    public void enviarNotificacion(AlertaFraude alerta) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        try {
            log.warn("Enviando notificación asíncrona para alerta CRITICA id: {} - motivo: {}",
                    alerta.getId(), alerta.getMotivo());

            // Aquí iría la lógica real de notificación:
            // email, SMS, webhook, etc.
            // Por ahora se simula con un log de nivel ERROR
            log.error("ALERTA CRITICA DETECTADA — Transacción id: {} — Motivo: {}",
                    alerta.getTransaccion().getId(), alerta.getMotivo());

        } finally {
            MDC.clear();
        }
    }

}
