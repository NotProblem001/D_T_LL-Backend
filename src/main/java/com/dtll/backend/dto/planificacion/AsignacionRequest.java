package com.dtll.backend.dto.planificacion;

import java.util.UUID;

/** Asignación (o reemplazo) de conductor y vehículo de un viaje. null = quitar. */
public record AsignacionRequest(UUID conductorId, UUID vehiculoId) {
}
