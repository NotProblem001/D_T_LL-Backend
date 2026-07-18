package com.dtll.backend.dto.maestros;

import com.dtll.backend.model.entity.Conductor;

import java.util.UUID;

/** Resumen para poblar selectores (conductor habitual de vehículos y rutas). */
public record ConductorResumenResponse(UUID id, String nombreCompleto, String rutConductor, Boolean activo) {

    public static ConductorResumenResponse desde(Conductor c) {
        return new ConductorResumenResponse(c.getId(), c.getNombreCompleto(), c.getRutConductor(), c.getActivo());
    }
}
