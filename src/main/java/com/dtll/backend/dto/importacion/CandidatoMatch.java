package com.dtll.backend.dto.importacion;

import java.util.UUID;

/** Pasajero sugerido para un registro importado, con su puntaje de similitud (0..1). */
public record CandidatoMatch(UUID pasajeroId, String nombre, double score) {
}
