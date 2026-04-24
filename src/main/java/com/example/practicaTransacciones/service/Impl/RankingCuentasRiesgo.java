package com.example.practicaTransacciones.service.Impl;

import com.example.practicaTransacciones.domain.Cuenta;
import com.example.practicaTransacciones.domain.NivelRiesgo;
import com.example.practicaTransacciones.dto.RankingCuentaResponseDTO;
import com.example.practicaTransacciones.repository.AlertaFraudeRepository;
import com.example.practicaTransacciones.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingCuentasRiesgo {
    private final CuentaRepository cuentaRepository;
    private final AlertaFraudeRepository alertaFraudeRepository;

    @Transactional(readOnly = true)
    public List<RankingCuentaResponseDTO> calcularRanking() {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Calculando ranking de cuentas por riesgo");

        // Obtener todas las cuentas con sus transacciones
        List<Cuenta> cuentas = cuentaRepository.findAllWithTransacciones();

        List<RankingCuentaResponseDTO> ranking = cuentas.stream()
                .map(this::buildResponse)
                .sorted(comparadorRiesgo())
                .toList();

        log.info("Ranking calculado para {} cuentas", ranking.size());
        MDC.clear();
        return ranking;
    }

    private RankingCuentaResponseDTO buildResponse(Cuenta cuenta) {
        // Score acumulado: suma de riesgoFraude de todas sus transacciones
        Double scoreAcumulado = cuenta.getTransacciones().stream()
                .mapToDouble(t -> t.getRiesgoFraude() != null ? t.getRiesgoFraude() : 0.0)
                .sum();

        // Número de alertas CRITICAS asociadas a sus transacciones
        long alertasCriticas = cuenta.getTransacciones().stream()
                .flatMap(t -> alertaFraudeRepository
                        .findByTransaccionIdAndNivel(t.getId(), NivelRiesgo.CRITICO)
                        .stream())
                .count();

        return new RankingCuentaResponseDTO(
                cuenta.getId(),
                cuenta.getNumeroCuenta(),
                cuenta.getSaldo(),
                scoreAcumulado,
                alertasCriticas,
                cuenta.getFechaAlta()
        );
    }

    // Algoritmo 3: Comparator con tres criterios en orden
    private Comparator<RankingCuentaResponseDTO> comparadorRiesgo() {
        return Comparator
                // Criterio 1: Score acumulado descendente (mayor riesgo primero)
                .comparingDouble(RankingCuentaResponseDTO::scoreRiesgoAcumulado).reversed()
                // Criterio 2: Alertas CRITICAS descendente (más alertas primero)
                .thenComparingLong(RankingCuentaResponseDTO::alertasCriticas).reversed()
                // Criterio 3: Antigüedad ascendente (cuentas nuevas primero)
                .thenComparing(RankingCuentaResponseDTO::fechaAlta);
    }
}

