package com.example.practicaTransacciones.service.Impl;

import com.example.practicaTransacciones.dto.TransaccionDTOResponse;
import com.example.practicaTransacciones.exception.TransaccionNotFoundException;
import com.example.practicaTransacciones.mapper.TransaccionMapper;
import com.example.practicaTransacciones.repository.TransaccionRepository;
import com.example.practicaTransacciones.service.TransaccionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransaccionServiceImpl implements TransaccionService {
    private final TransaccionRepository transaccionRepository;
    private final TransaccionMapper transaccionMapper;

    //UUID -> Valores de 128 bits que son ids únicos globalmente
    //MDC -> Añade datos a los logs generados en una ejecución
    @Transactional(readOnly = true)
    public TransaccionDTOResponse obtenerEstado(Long id) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        //Buca en bbdd transacciones con id ( devuelve Optional)
        var transaccion = transaccionRepository.findById(id)
                //Si está vacío lanza exception (404)
                .orElseThrow(() -> new TransaccionNotFoundException(id));

        log.info("Transacción con id:" + id);
        MDC.clear();
        return transaccionMapper.toEstadoResponse(transaccion);
    }
}

