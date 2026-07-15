package com.dtll.backend.dto.empresa;

public record ResumenEmpresaResponse(
        long viajesEsteMes,
        long pasajerosActivos,
        int mesFiscal,
        int anioFiscal
) {
}
