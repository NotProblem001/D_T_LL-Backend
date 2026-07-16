package com.dtll.backend.dto.empresa;

import java.util.UUID;

/** Item liviano para el selector de empresas del ADMIN en el dashboard. */
public record EmpresaListItemResponse(UUID id, String nombre) {
}
