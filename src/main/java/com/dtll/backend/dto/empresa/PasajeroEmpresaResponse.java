package com.dtll.backend.dto.empresa;

import java.util.UUID;

public record PasajeroEmpresaResponse(
        UUID id,
        String identificadorInterno,
        String nombreCompleto,
        String comuna,
        String puntoParadaAsignado,
        boolean activo
) {
}
