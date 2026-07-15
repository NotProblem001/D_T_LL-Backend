package com.dtll.backend.dto.empresa;

import java.math.BigDecimal;
import java.util.UUID;

public record ReporteFacturacionResponse(
        UUID id,
        int mesFiscal,
        int anioFiscal,
        int totalViajesEjecutados,
        BigDecimal montoExentoTotal,
        String estadoDocumento
) {
}
