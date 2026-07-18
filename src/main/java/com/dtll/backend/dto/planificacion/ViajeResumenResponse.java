package com.dtll.backend.dto.planificacion;

import com.dtll.backend.model.enums.EstadoViaje;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record ViajeResumenResponse(
        UUID id,
        String codigoRutaLogin,
        LocalDate fechaOperacion,
        String jornadaTurno,
        String tipoTrayecto,
        EstadoViaje estado,
        UUID rutaId,
        String rutaNombre,
        UUID conductorId,
        String conductorNombre,
        UUID vehiculoId,
        String vehiculoPatente,
        Integer vehiculoCapacidad,
        long totalPasajeros,
        LocalTime horaProgramadaInicio,
        LocalTime horaProgramadaTermino,
        List<String> alertas) {
}
