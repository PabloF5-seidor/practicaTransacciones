package com.example.practicaTransacciones.domain;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cuenta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 50)
    private String numeroCuenta;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCuenta tipo;

    @Column(name = "fecha_alta", nullable = false,columnDefinition = "DATE")
    private LocalDate fechaAlta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCuenta estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "cuentaOrigenRef", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Transaccion> transacciones = new ArrayList<>();
}
