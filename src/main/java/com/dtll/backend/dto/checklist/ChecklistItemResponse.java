package com.dtll.backend.dto.checklist;

import com.dtll.backend.model.enums.EstadoAsistencia;

import java.util.UUID;

public record ChecklistItemResponse(
        UUID asistenciaId,
        UUID pasajeroId,
        String nombreCompleto,
        String puntoParadaAsignado,
        EstadoAsistencia estado
) {
}
