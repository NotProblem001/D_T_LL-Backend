package com.dtll.backend.dto.importacion;

import java.util.UUID;

public record PreviewTextoRequest(UUID empresaId, String texto, Integer anio, Integer semana) {
}
