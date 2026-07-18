package com.dtll.backend.dto.historial;

import com.dtll.backend.model.entity.AsistenciaHistorial;

import java.time.LocalDateTime;

/** Corrección registrada sobre una asistencia (valor anterior → nuevo, motivo). */
public record AsistenciaHistorialResponse(
        String valorAnterior,
        String valorNuevo,
        String motivo,
        String usuarioRol,
        LocalDateTime createdAt) {

    public static AsistenciaHistorialResponse desde(AsistenciaHistorial h) {
        return new AsistenciaHistorialResponse(h.getValorAnterior(), h.getValorNuevo(),
                h.getMotivo(), h.getUsuarioRol(), h.getCreatedAt());
    }
}
