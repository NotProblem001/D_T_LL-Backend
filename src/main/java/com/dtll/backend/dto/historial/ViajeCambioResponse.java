package com.dtll.backend.dto.historial;

import com.dtll.backend.model.entity.ViajeCambio;

import java.time.LocalDateTime;
import java.util.UUID;

public record ViajeCambioResponse(
        UUID id,
        String campo,
        String valorAnterior,
        String valorNuevo,
        String motivo,
        String usuarioRol,
        LocalDateTime createdAt) {

    public static ViajeCambioResponse desde(ViajeCambio c) {
        return new ViajeCambioResponse(c.getId(), c.getCampo(), c.getValorAnterior(),
                c.getValorNuevo(), c.getMotivo(), c.getUsuarioRol(), c.getCreatedAt());
    }
}
