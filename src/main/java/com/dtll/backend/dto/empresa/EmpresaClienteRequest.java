package com.dtll.backend.dto.empresa;

import java.math.BigDecimal;

/** Datos para crear o actualizar una empresa cliente desde el panel de administración. */
public record EmpresaClienteRequest(
        String rutFiscal,
        String razonSocial,
        String nombreFantasia,
        String contactoNombre,
        String contactoEmail,
        String contactoTelefono,
        BigDecimal tarifaBaseViaje) {
}
