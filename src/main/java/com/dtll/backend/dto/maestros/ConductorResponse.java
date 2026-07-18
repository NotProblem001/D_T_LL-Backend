package com.dtll.backend.dto.maestros;

import com.dtll.backend.model.entity.Conductor;
import com.dtll.backend.model.enums.TipoContrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ConductorResponse(
        UUID id,
        String rutConductor,
        String nombreCompleto,
        String telefono,
        String email,
        TipoContrato tipoContrato,
        BigDecimal tarifaPorViaje,
        String tipoLicencia,
        LocalDate fechaVencimientoLicencia,
        boolean licenciaVencida,
        boolean tienePin,
        String observaciones,
        Boolean activo) {

    public static ConductorResponse desde(Conductor c) {
        return new ConductorResponse(
                c.getId(),
                c.getRutConductor(),
                c.getNombreCompleto(),
                c.getTelefono(),
                c.getEmail(),
                c.getTipoContrato(),
                c.getTarifaPorViaje(),
                c.getTipoLicencia(),
                c.getFechaVencimientoLicencia(),
                c.getFechaVencimientoLicencia() != null
                        && c.getFechaVencimientoLicencia().isBefore(LocalDate.now()),
                c.getPinAccesoHash() != null,
                c.getObservaciones(),
                c.getActivo());
    }
}
