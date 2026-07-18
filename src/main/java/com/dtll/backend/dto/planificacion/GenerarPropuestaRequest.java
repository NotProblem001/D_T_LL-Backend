package com.dtll.backend.dto.planificacion;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Genera la propuesta de recorridos de una fecha a partir de la nómina semanal
 * importada (anio/semana) agrupando pasajeros por ruta.
 */
public record GenerarPropuestaRequest(
        UUID empresaId,
        Integer anio,
        Integer semana,
        LocalDate fecha) {
}
