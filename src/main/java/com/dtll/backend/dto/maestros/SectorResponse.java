package com.dtll.backend.dto.maestros;

import com.dtll.backend.model.entity.Sector;

import java.util.List;
import java.util.UUID;

public record SectorResponse(
        UUID id,
        String nombre,
        String descripcion,
        Boolean activo,
        List<ComunaResponse> comunas) {

    public static SectorResponse desde(Sector s) {
        return new SectorResponse(
                s.getId(),
                s.getNombre(),
                s.getDescripcion(),
                s.getActivo(),
                s.getComunas().stream().map(ComunaResponse::desde).toList());
    }
}
