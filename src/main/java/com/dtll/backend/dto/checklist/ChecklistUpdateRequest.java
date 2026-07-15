package com.dtll.backend.dto.checklist;

import com.dtll.backend.model.enums.EstadoAsistencia;

import java.util.List;
import java.util.UUID;

public record ChecklistUpdateRequest(List<Item> marcaciones) {
    public record Item(UUID asistenciaId, EstadoAsistencia estado, String observaciones) {
    }
}
