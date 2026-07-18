package com.dtll.backend.dto.importacion;

import com.dtll.backend.model.enums.ResolucionRegistro;

import java.util.UUID;

/**
 * Decisión del operador sobre un registro: ACEPTADO exige pasajeroId
 * (el sugerido u otro elegido a mano); NUEVO crea pasajero al confirmar;
 * DESCARTADO lo excluye de la importación.
 */
public record ResolverRegistroRequest(ResolucionRegistro resolucion, UUID pasajeroId) {
}
