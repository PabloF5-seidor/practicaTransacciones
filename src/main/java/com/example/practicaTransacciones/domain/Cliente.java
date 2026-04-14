package com.example.practicaTransacciones.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;

import java.util.List;


@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "fecha_alta", nullable = false,columnDefinition = "DATE")
    private LocalDate fechaAlta;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Cuenta> cuentas = new ArrayList<>();
}