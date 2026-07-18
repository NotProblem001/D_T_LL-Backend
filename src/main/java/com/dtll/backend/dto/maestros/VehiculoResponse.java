package com.dtll.backend.dto.maestros;

import com.dtll.backend.model.entity.Vehiculo;
import com.dtll.backend.model.enums.EstadoVehiculo;

import java.time.LocalDate;
import java.util.UUID;

public record VehiculoResponse(
        UUID id,
        String patente,
        String marca,
        String modelo,
        Integer anio,
        Integer capacidadPasajeros,
        String tipoVehiculo,
        EstadoVehiculo estado,
        UUID conductorHabitualId,
        String conductorHabitualNombre,
        Integer kilometraje,
        LocalDate fechaRevisionTecnica,
        LocalDate fechaPermisoCirculacion,
        LocalDate fechaVencimientoSeguro,
        boolean documentosVencidos,
        String observaciones,
        Boolean activo) {

    public static VehiculoResponse desde(Vehiculo v) {
        return new VehiculoResponse(
                v.getId(),
                v.getPatente(),
                v.getMarca(),
                v.getModelo(),
                v.getAnio(),
                v.getCapacidadPasajeros(),
                v.getTipoVehiculo(),
                v.getEstado(),
                v.getConductorHabitual() != null ? v.getConductorHabitual().getId() : null,
                v.getConductorHabitual() != null ? v.getConductorHabitual().getNombreCompleto() : null,
                v.getKilometraje(),
                v.getFechaRevisionTecnica(),
                v.getFechaPermisoCirculacion(),
                v.getFechaVencimientoSeguro(),
                v.documentosVencidos(LocalDate.now()),
                v.getObservaciones(),
                v.getActivo());
    }
}
