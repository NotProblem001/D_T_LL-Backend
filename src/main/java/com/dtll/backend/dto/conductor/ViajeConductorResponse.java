package com.dtll.backend.dto.conductor;

import com.dtll.backend.model.entity.Viaje;
import com.dtll.backend.model.enums.EstadoViaje;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/** Vista liviana de un recorrido para el teléfono del conductor. */
public record ViajeConductorResponse(
        UUID id,
        String codigoRutaLogin,
        LocalDate fechaOperacion,
        String jornadaTurno,
        String tipoTrayecto,
        EstadoViaje estado,
        String rutaNombre,
        String vehiculoPatente,
        LocalTime horaProgramadaInicio,
        LocalTime horaProgramadaTermino,
        LocalDateTime horaRealInicio,
        LocalDateTime horaRealTermino,
        long totalPasajeros,
        long pendientes) {

    public static ViajeConductorResponse desde(Viaje v, long totalPasajeros, long pendientes) {
        return new ViajeConductorResponse(
                v.getId(),
                v.getCodigoRutaLogin(),
                v.getFechaOperacion(),
                v.getJornadaTurno(),
                v.getTipoTrayecto(),
                v.getEstado(),
                v.getRuta() != null ? v.getRuta().getNombre() : null,
                v.getVehiculo() != null ? v.getVehiculo().getPatente() : null,
                v.getHoraProgramadaInicio(),
                v.getHoraProgramadaTermino(),
                v.getHoraRealInicio(),
                v.getHoraRealTermino(),
                totalPasajeros,
                pendientes);
    }
}
