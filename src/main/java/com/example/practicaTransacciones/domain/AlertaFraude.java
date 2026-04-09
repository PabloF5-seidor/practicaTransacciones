package com.example.practicaTransacciones.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alerta_fraude")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaFraude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaccion_id", nullable = false)
    private Transaccion transaccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NivelRiesgo nivel;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Column(nullable = false)
    private Boolean revisada;
}
