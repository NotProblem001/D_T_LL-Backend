package com.dtll.backend.model.enums;

/** Decisión del operador (o automática) sobre un registro importado. */
public enum ResolucionRegistro {
    PENDIENTE, // sugerencia sin resolver: bloquea la confirmación
    ACEPTADO,  // vinculado a un pasajero existente
    NUEVO,     // crear pasajero nuevo al confirmar
    DESCARTADO // no se importa (duplicado, error, decisión del operador)
}
