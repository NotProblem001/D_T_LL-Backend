package com.dtll.backend.dto.historial;

import com.dtll.backend.model.enums.EstadoViaje;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Fila del historial de recorridos. Para viajes cerrados los datos vienen del
 * snapshot congelado; para viajes vigentes, de las referencias actuales.
 */
public record HistorialViajeResponse(
        UUID id,
        String codigoRutaLogin,
        LocalDate fechaOperacion,
        String jornadaTurno,
        String tipoTrayecto,
        EstadoViaje estado,
        String rutaNombre,
        String conductorNombre,
        String vehiculoPatente,
        LocalTime horaProgramadaInicio,
        LocalDateTime horaRealInicio,
        LocalDateTime horaRealTermino,
        Integer totalPasajeros,
        Integer totalTransportados,
        Integer totalAusentes,
        Integer totalCancelaciones,
        long cambios,
        long incidencias) {
}
