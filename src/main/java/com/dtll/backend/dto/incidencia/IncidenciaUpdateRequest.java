package com.dtll.backend.dto.incidencia;

import com.dtll.backend.model.enums.EstadoIncidencia;

/** Gestión de la incidencia por el operador: estado y acción realizada. */
public record IncidenciaUpdateRequest(EstadoIncidencia estado, String accionRealizada) {
}
