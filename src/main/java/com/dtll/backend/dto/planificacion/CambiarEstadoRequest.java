package com.dtll.backend.dto.planificacion;

import com.dtll.backend.model.enums.EstadoViaje;

public record CambiarEstadoRequest(EstadoViaje estado) {
}
