package com.dtll.backend.dto.maestros;

import com.dtll.backend.model.entity.Comuna;

import java.util.UUID;

public record ComunaResponse(UUID id, String nombre, Boolean activo) {

    public static ComunaResponse desde(Comuna c) {
        return new ComunaResponse(c.getId(), c.getNombre(), c.getActivo());
    }
}
