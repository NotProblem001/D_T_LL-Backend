package com.dtll.backend.optimization.dto;

import java.util.UUID;

public record ParadaOptimizadaResponse(
        UUID pasajeroId,
        String nombreCompleto,
        String puntoParadaAsignado,
        int ordenParada,
        double distanciaAcumuladaM,
        int tiempoEstimadoSeg,
        double lat,
        double lng
) {
}
