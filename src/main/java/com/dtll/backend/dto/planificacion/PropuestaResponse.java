package com.dtll.backend.dto.planificacion;

import java.util.List;

public record PropuestaResponse(
        List<ViajeResumenResponse> viajes,
        List<PasajeroSinRutaResponse> sinRuta,
        List<String> avisos) {
}
