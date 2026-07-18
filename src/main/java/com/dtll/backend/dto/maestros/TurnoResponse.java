package com.dtll.backend.dto.maestros;

import com.dtll.backend.model.entity.Turno;
import com.dtll.backend.model.enums.TipoTrayecto;

import java.time.LocalTime;
import java.util.UUID;

public record TurnoResponse(
        UUID id,
        String nombre,
        TipoTrayecto tipoServicio,
        LocalTime horaInicio,
        LocalTime horaLlegadaEstimada,
        String diasSemana,
        Boolean activo) {

    public static TurnoResponse desde(Turno t) {
        return new TurnoResponse(
                t.getId(),
                t.getNombre(),
                t.getTipoServicio(),
                t.getHoraInicio(),
                t.getHoraLlegadaEstimada(),
                t.getDiasSemana(),
                t.getActivo());
    }
}
