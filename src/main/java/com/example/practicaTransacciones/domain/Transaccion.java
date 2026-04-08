package com.example.practicaTransacciones.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.Instant;
@Entity
@Table(name="transacciones")
public class Transaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String cuentaOrigen;
    @Column(nullable = false)
    private String cuentaDestino;
    @Column(nullable = false)
    private BigDecimal monto;
    @Column(nullable = false)
    private TipoTransaccion tipo;
    @Column(nullable = false)
    private EstadoCuenta estado;
    @Column(nullable = false)
    private Instant fechaHora;
    private String descripcion;
    private Double riesgoFraude;
}
