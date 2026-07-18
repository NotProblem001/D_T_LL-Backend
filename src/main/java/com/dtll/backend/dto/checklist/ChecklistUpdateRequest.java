package com.dtll.backend.dto.checklist;

import java.util.List;
import java.util.UUID;

public record ChecklistUpdateRequest(List<Item> marcaciones) {

    /**
     * estado: código del maestro estados_asistencia (o PENDIENTE).
     * motivo: obligatorio solo al corregir una asistencia ya marcada (queda en historial).
     */
    public record Item(UUID asistenciaId, String estado, String observaciones, String motivo) {
    }
}
