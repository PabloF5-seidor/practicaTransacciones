package com.example.practicaTransacciones.service.Impl;

import com.example.practicaTransacciones.domain.Transaccion;
import com.example.practicaTransacciones.dto.CuentaDTOResponse;
import com.example.practicaTransacciones.exception.CuentaNotFoundException;
import com.example.practicaTransacciones.repository.CuentaRepository;
import com.example.practicaTransacciones.repository.TransaccionRepository;
import com.example.practicaTransacciones.service.CuentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Slf4j //log. MDC
@Service
@RequiredArgsConstructor
public class CuentaServiceImpl implements CuentaService {
    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;


    private BigDecimal calcularPromedio(List<Transaccion> transacciones){
        // Con menos de 2 transacciones la desviación no tiene sentido devuelve 0.
        if (transacciones.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal suma = transacciones.stream()
                .map(Transaccion::getMonto)//Convierte transacción a BigDecimal (monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);//Suma montos
        //Divide la suma entre el número de transacciones, redondeando a 2 decimales
        return suma.divide(BigDecimal.valueOf(transacciones.size()),2, RoundingMode.HALF_UP);

    }
    @Transactional(readOnly = true)
    public CuentaDTOResponse resumenCuenta(Long cuentaId){
        //Añade datos a logs durante ejecucion, util cuando hay muchos hilos
        MDC.put("correlationId", UUID.randomUUID().toString());
        //Entrada al metodo
        //{} insterta cuentaId
        log.info("Resumen cuenta {}", cuentaId);

        //Busca cuenta con ese id, devuelve optional<>
        var cuenta = cuentaRepository.findById(cuentaId).orElseThrow(() -> new CuentaNotFoundException(cuentaId));
        //Obtiene transacciones de esa cuenta por id de cuenta origen
        List<Transaccion> transacciones = transaccionRepository.findByCuentaOrigenRefId(cuentaId);

        //Calculo monto promedio
        BigDecimal montoPromedio = calcularPromedio(transacciones);
        //Necesita transacciones y el promedio para formula
        BigDecimal desviacionEstandar = calcularDesviacion(transacciones, montoPromedio);
        //Suma los scores de fraude de las transacciones
        Double puntuacionRiesgoAcumulada = calcularRiesgoAcumulado(transacciones);

        //Log salida metodo con resultados
        log.info("Resumen calculado para cuenta id: {} - movimientos: {}", cuentaId, transacciones.size());
        MDC.clear();//Limpia MDC para que siguiente hilo se pueda reusar
        //Devolución datos
        return new CuentaDTOResponse(
                cuenta.getId(),
                cuenta.getNumeroCuenta(),
                cuenta.getSaldo(),
                transacciones.size(),
                montoPromedio,
                desviacionEstandar,
                puntuacionRiesgoAcumulada
        );

    }

    private BigDecimal calcularDesviacion(List<Transaccion> transacciones, BigDecimal montoPromedio) {
        // Con menos de 2 transacciones la desviación  devuelve 0

        if(transacciones.size()<2){
            return BigDecimal.ZERO;
        } BigDecimal sum = transacciones.stream()
                // Por cada transacción promedio= (monto - promedio)
                .map(t -> t.getMonto().subtract(montoPromedio).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //suma los cuadrados
        //Calculo varianza suma(monto - promedio)^ 2 /n
        BigDecimal des = sum.divide(BigDecimal.valueOf(transacciones.size()),10,RoundingMode.HALF_UP);
        //Calculo raiz cuadrada de varianza = desviacion
        return des.sqrt(new MathContext(10, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);//Redondea a 2 decimales
    }
    private Double calcularRiesgoAcumulado(List<Transaccion> transacciones) {
        //Convierte transaccion a el valor de riesgoFraude si es null usa 0 y evita exception
        return transacciones.stream().mapToDouble(t -> t.getRiesgoFraude() !=null ? t.getRiesgoFraude():0).sum();
        //Suma todos los valores de riesgo acumulados
    }
}
