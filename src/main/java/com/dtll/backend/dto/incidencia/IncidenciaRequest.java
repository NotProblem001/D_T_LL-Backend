package com.dtll.backend.dto.incidencia;

import java.util.UUID;

/**
 * Alta de una incidencia. Todas las asociaciones son opcionales; si la reporta
 * un conductor con viaje, el conductor y vehículo se completan solos.
 */
public record IncidenciaRequest(
        UUID viajeId,
        UUID pasajeroId,
        UUID conductorId,
        UUID vehiculoId,
        String tipo,
        String descripcion) {
}
