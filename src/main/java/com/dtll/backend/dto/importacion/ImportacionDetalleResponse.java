package com.dtll.backend.dto.importacion;

import java.util.List;

public record ImportacionDetalleResponse(
        ImportacionResponse resumen,
        List<RegistroImportacionResponse> registros) {
}
