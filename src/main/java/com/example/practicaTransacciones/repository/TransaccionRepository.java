package com.example.practicaTransacciones.repository;

import com.example.practicaTransacciones.domain.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {


}
