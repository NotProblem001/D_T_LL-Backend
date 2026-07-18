package com.dtll.backend.model.enums;

/** Ciclo de vida completo de un recorrido (sección 12 del requerimiento). */
public enum EstadoViaje {
    BORRADOR,     // propuesta generada, sin asignación completa
    PROGRAMADO,   // planificado por el operador
    ASIGNADO,     // con conductor y vehículo
    CONFIRMADO,   // el conductor aceptó la asignación (Etapa 4)
    EN_CURSO,
    FINALIZADO,
    CANCELADO,
    REPROGRAMADO
}
