package com.dtll.backend.dto.maestros;

import java.util.List;
import java.util.UUID;

public record RutaRequest(
        UUID empresaId,
        String nombre,
        String descripcion,
        List<UUID> sectorIds,
        UUID conductorHabitualId,
        UUID vehiculoHabitualId) {
}
