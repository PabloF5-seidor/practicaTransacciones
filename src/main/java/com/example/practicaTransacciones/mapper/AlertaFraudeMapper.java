package com.example.practicaTransacciones.mapper;

import com.example.practicaTransacciones.domain.AlertaFraude;
import com.example.practicaTransacciones.dto.AlertaFraudeResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface AlertaFraudeMapper {
    @Mapping(source = "transaccion.id", target = "transaccionId")
    @Mapping(source = "transaccion.fechaHora", target = "fechaHora")
    AlertaFraudeResponseDTO toResponse(AlertaFraude alertaFraude);
}
