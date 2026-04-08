package com.example.practicaTransacciones.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Entity
@Table(name = "clientes")

public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false,unique = true)
    private String dni;
    @Column(nullable = false,unique = true)
    private String email;
    @Column(nullable = false)
   private Date fechaAlta;
}
