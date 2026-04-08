package com.example.practicaTransacciones.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
@Entity
@Table(name = "cuentas")
public class Cuenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false,unique = true)
    private String numeroCuenta;
    @Column(nullable = false)
    private BigDecimal saldo;
    @Column(nullable = false)
    private TipoCuenta tipo;
    @Column(nullable = false)
    private EstadoCuenta estado;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Long clienteId;
}
