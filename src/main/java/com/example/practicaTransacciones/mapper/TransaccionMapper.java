package com.example.practicaTransacciones.mapper;

import com.example.practicaTransacciones.domain.Transaccion;
import com.example.practicaTransacciones.dto.TransaccionDTOResponse;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface TransaccionMapper {
    TransaccionDTOResponse toEstadoResponse(Transaccion transaccion);
}
