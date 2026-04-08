package com.example.practicaTransacciones.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "alertas_fraude")
public class AlertaFraude {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaccion_id", nullable = false)
    private Long transaccionId;
    @Column(nullable = false)
    private NivelRiesgo nivel;
    @Column(nullable = false)
    private String motivo;
    @Column(nullable = false)
    private Boolean revisar;
}
