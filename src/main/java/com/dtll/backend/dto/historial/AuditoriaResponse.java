package com.dtll.backend.dto.historial;

import com.dtll.backend.model.entity.Auditoria;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditoriaResponse(
        UUID id,
        String usuarioRol,
        String accion,
        String modulo,
        UUID registroId,
        String descripcion,
        String datosAnterior,
        String datosNuevo,
        LocalDateTime createdAt) {

    public static AuditoriaResponse desde(Auditoria a) {
        return new AuditoriaResponse(a.getId(), a.getUsuarioRol(), a.getAccion(), a.getModulo(),
                a.getRegistroId(), a.getDescripcion(), a.getDatosAnterior(), a.getDatosNuevo(),
                a.getCreatedAt());
    }
}
