package com.dtll.backend.dto.historial;

import java.util.List;
import java.util.UUID;

/** Pasajero del recorrido con su asistencia (datos del snapshot si el viaje está cerrado). */
public record PasajeroHistorialResponse(
        UUID pasajeroId,
        String nombre,
        String telefono,
        String direccion,
        String estadoAsistencia,
        String observaciones,
        List<AsistenciaHistorialResponse> correcciones) {
}
