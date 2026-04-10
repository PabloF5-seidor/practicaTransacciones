package com.example.practicaTransacciones.repository;

import com.example.practicaTransacciones.domain.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    @Query("SELECT t FROM Transaccion t WHERE t.cuentaOrigen.id = :cuentaId")
    List<Transaccion> findByCuentaOrigenId(@Param("cuentaId") Long cuentaId);

}
