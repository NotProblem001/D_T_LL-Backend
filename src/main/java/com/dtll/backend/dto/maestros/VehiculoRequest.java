package com.dtll.backend.dto.maestros;

import com.dtll.backend.model.enums.EstadoVehiculo;

import java.time.LocalDate;
import java.util.UUID;

public record VehiculoRequest(
        String patente,
        String marca,
        String modelo,
        Integer anio,
        Integer capacidadPasajeros,
        String tipoVehiculo,
        EstadoVehiculo estado,
        UUID conductorHabitualId,
        Integer kilometraje,
        LocalDate fechaRevisionTecnica,
        LocalDate fechaPermisoCirculacion,
        LocalDate fechaVencimientoSeguro,
        String observaciones) {
}
