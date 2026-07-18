package com.dtll.backend.dto.maestros;

import com.dtll.backend.model.enums.TipoContrato;

import java.math.BigDecimal;
import java.time.LocalDate;

/** pin: opcional; si viene vacío en una edición, se conserva el PIN actual. */
public record ConductorRequest(
        String rutConductor,
        String nombreCompleto,
        String telefono,
        String email,
        TipoContrato tipoContrato,
        BigDecimal tarifaPorViaje,
        String tipoLicencia,
        LocalDate fechaVencimientoLicencia,
        String observaciones,
        String pin) {
}
