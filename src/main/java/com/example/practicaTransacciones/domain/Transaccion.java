package com.example.practicaTransacciones.domain;



import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_origen", nullable = false, length = 50)
    private String cuentaOrigen;

    @Column(name = "cuenta_destino", nullable = false, length = 50)
    private String cuentaDestino;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTransaccion tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTransaccion estado;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    private String descripcion;

    private Double riesgoFraude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_origen_id")
    private Cuenta cuentaOrigenRef;
}