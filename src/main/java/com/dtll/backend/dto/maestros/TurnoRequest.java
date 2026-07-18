package com.dtll.backend.dto.maestros;

import com.dtll.backend.model.enums.TipoTrayecto;

import java.time.LocalTime;

public record TurnoRequest(
        String nombre,
        TipoTrayecto tipoServicio,
        LocalTime horaInicio,
        LocalTime horaLlegadaEstimada,
        String diasSemana) {
}
