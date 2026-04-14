package com.example.practicaTransacciones.repository;

import com.example.practicaTransacciones.domain.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    @Query("SELECT t FROM Transaccion t WHERE t.cuentaOrigenRef.id = :cuentaId")
    List<Transaccion> findByCuentaOrigenRefId(@Param("cuentaId") Long cuentaId);

    // Cuenta transacciones recientes para el score de fraude (indicador 3)
    @Query("SELECT COUNT(t) FROM Transaccion t WHERE t.cuentaOrigen = :numeroCuenta AND t.fechaHora >= :desde")
    long countTransaccionesRecientes(@Param("numeroCuenta") String numeroCuenta,
                                     @Param("desde") LocalDateTime desde);

    // Historial de transacciones para el score de fraude (indicador 5)
    @Query("SELECT t FROM Transaccion t WHERE t.cuentaOrigen = :numeroCuenta")
    List<Transaccion> findByCuentaOrigen(@Param("numeroCuenta") String numeroCuenta);

}
