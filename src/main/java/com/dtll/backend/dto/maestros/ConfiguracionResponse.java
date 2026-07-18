package com.dtll.backend.dto.maestros;

import com.dtll.backend.model.entity.Configuracion;

public record ConfiguracionResponse(String clave, String valor, String descripcion) {

    public static ConfiguracionResponse desde(Configuracion c) {
        return new ConfiguracionResponse(c.getClave(), c.getValor(), c.getDescripcion());
    }
}
