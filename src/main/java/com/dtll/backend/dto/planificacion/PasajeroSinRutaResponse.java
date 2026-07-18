package com.dtll.backend.dto.planificacion;

import java.util.UUID;

/** Pasajero de la nómina que no pudo asignarse a ninguna ruta (o está excluido). */
public record PasajeroSinRutaResponse(
        UUID pasajeroId,
        String nombre,
        String comuna,
        String turno,
        String motivo) {
}
