package com.dtll.backend.dto.historial;

import com.dtll.backend.dto.incidencia.IncidenciaResponse;

import java.util.List;

/** Detalle completo de un recorrido histórico (sección 15). */
public record HistorialDetalleResponse(
        HistorialViajeResponse viaje,
        List<PasajeroHistorialResponse> pasajeros,
        List<ViajeCambioResponse> cambios,
        List<IncidenciaResponse> incidencias) {
}
