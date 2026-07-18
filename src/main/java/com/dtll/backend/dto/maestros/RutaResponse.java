package com.dtll.backend.dto.maestros;

import com.dtll.backend.model.entity.Ruta;

import java.util.List;
import java.util.UUID;

public record RutaResponse(
        UUID id,
        UUID empresaId,
        String nombre,
        String descripcion,
        List<SectorResponse> sectores,
        UUID conductorHabitualId,
        String conductorHabitualNombre,
        UUID vehiculoHabitualId,
        String vehiculoHabitualPatente,
        Boolean activo) {

    public static RutaResponse desde(Ruta r) {
        return new RutaResponse(
                r.getId(),
                r.getEmpresaCliente().getId(),
                r.getNombre(),
                r.getDescripcion(),
                r.getSectores().stream().map(SectorResponse::desde).toList(),
                r.getConductorHabitual() != null ? r.getConductorHabitual().getId() : null,
                r.getConductorHabitual() != null ? r.getConductorHabitual().getNombreCompleto() : null,
                r.getVehiculoHabitual() != null ? r.getVehiculoHabitual().getId() : null,
                r.getVehiculoHabitual() != null ? r.getVehiculoHabitual().getPatente() : null,
                r.getActivo());
    }
}
