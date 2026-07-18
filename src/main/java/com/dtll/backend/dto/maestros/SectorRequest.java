package com.dtll.backend.dto.maestros;

import java.util.List;
import java.util.UUID;

public record SectorRequest(String nombre, String descripcion, List<UUID> comunaIds) {
}
