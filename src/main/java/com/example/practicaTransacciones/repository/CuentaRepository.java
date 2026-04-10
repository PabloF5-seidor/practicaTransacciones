package com.example.practicaTransacciones.repository;

import com.example.practicaTransacciones.domain.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
}
