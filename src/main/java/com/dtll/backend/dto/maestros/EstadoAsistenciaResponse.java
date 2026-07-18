package com.dtll.backend.dto.maestros;

import com.dtll.backend.model.entity.EstadoAsistenciaConfig;

import java.util.UUID;

public record EstadoAsistenciaResponse(
        UUID id,
        String codigo,
        String nombre,
        Boolean requiereObservacion,
        Integer orden,
        Boolean activo) {

    public static EstadoAsistenciaResponse desde(EstadoAsistenciaConfig e) {
        return new EstadoAsistenciaResponse(
                e.getId(),
                e.getCodigo(),
                e.getNombre(),
                e.getRequiereObservacion(),
                e.getOrden(),
                e.getActivo());
    }
}
