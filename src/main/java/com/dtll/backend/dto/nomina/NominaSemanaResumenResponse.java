package com.dtll.backend.dto.nomina;

import java.util.Map;

/** Semana con nómina importada y sus totales por turno, para los selectores del dashboard. */
public record NominaSemanaResumenResponse(
        int anio,
        int semana,
        long total,
        Map<String, Long> porTurno) {
}
