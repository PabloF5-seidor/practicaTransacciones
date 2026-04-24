package com.example.practicaTransacciones.repository;

import com.example.practicaTransacciones.domain.AlertaFraude;
import com.example.practicaTransacciones.domain.NivelRiesgo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlertaFraudeRepository extends JpaRepository<AlertaFraude, Long> {
    //Pageable -> parametro entrada recibe el cliente
    //Page-> Respuesta
    @Query("SELECT a FROM AlertaFraude a WHERE a.revisada = false " +
            "ORDER BY CASE a.nivel " +
            "WHEN 'CRITICO' THEN 1 " +
            "WHEN 'ALTO' THEN 2 " +
            "WHEN 'MEDIO' THEN 3 " +
            "WHEN 'BAJO' THEN 4 END ASC, " +
            "a.transaccion.fechaHora DESC")
    Page<AlertaFraude> findByRevisadaFalse(Pageable pageable);


    @Query("SELECT a FROM AlertaFraude a WHERE a.transaccion.id = :transaccionId AND a.nivel = :nivel")
    List<AlertaFraude> findByTransaccionIdAndNivel(
            @Param("transaccionId") Long transaccionId,
            @Param("nivel") NivelRiesgo nivel);
}
