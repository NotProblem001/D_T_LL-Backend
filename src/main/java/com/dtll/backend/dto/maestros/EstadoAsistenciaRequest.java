package com.dtll.backend.dto.maestros;

public record EstadoAsistenciaRequest(
        String codigo,
        String nombre,
        Boolean requiereObservacion,
        Integer orden) {
}
