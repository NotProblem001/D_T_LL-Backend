package com.dtll.backend.dto.informes;

/** Indicadores del panel principal (sección 18): el día consultado + su semana. */
public record DashboardResponse(
        Indicadores hoy,
        SemanaResumen semana,
        long conductoresActivos,
        long vehiculosDisponibles,
        long incidenciasAbiertas) {

    public record Indicadores(
            long pasajerosProgramados,
            long pasajerosTransportados,
            long pasajerosAusentes,
            long recorridosProgramados,
            long recorridosEnCurso,
            long recorridosFinalizados,
            long recorridosCancelados) {
    }

    public record SemanaResumen(
            long recorridos,
            long recorridosFinalizados,
            long pasajerosProgramados,
            long pasajerosTransportados,
            long pasajerosAusentes,
            double porcentajeAsistencia) {
    }
}
