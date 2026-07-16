package com.dtll.backend.dto.empresa;

import com.dtll.backend.model.entity.EmpresaCliente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Detalle de una empresa cliente para el módulo de Clientes del Admin. */
public record EmpresaClienteResponse(
        UUID id,
        String rutFiscal,
        String razonSocial,
        String nombreFantasia,
        String contactoNombre,
        String contactoEmail,
        String contactoTelefono,
        BigDecimal tarifaBaseViaje,
        LocalDateTime createdAt) {

    public static EmpresaClienteResponse desde(EmpresaCliente e) {
        return new EmpresaClienteResponse(
                e.getId(),
                e.getRutFiscal(),
                e.getRazonSocial(),
                e.getNombreFantasia(),
                e.getContactoNombre(),
                e.getContactoEmail(),
                e.getContactoTelefono(),
                e.getTarifaBaseViaje(),
                e.getCreatedAt());
    }
}
