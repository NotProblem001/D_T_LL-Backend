package com.dtll.backend.dto.checklist;

import java.util.UUID;

/** Item del checklist del conductor: pasajero + contacto + estado de asistencia. */
public record ChecklistItemResponse(
        UUID asistenciaId,
        UUID pasajeroId,
        String nombreCompleto,
        String puntoParadaAsignado,
        String telefono,
        String direccion,
        String comuna,
        String estado,
        String observaciones
) {
}
