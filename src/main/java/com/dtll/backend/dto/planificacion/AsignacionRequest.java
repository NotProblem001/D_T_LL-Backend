package com.dtll.backend.dto.planificacion;

import java.util.UUID;

/**
 * Asignación (o reemplazo) de conductor y vehículo de un viaje. null = quitar.
 * motivo: obligatorio al reemplazar una asignación existente (queda en viaje_cambios).
 */
public record AsignacionRequest(UUID conductorId, UUID vehiculoId, String motivo) {
}
