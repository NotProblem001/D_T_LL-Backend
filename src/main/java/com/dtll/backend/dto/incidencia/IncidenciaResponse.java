package com.dtll.backend.dto.incidencia;

import com.dtll.backend.model.entity.Incidencia;
import com.dtll.backend.model.enums.EstadoIncidencia;

import java.time.LocalDateTime;
import java.util.UUID;

public record IncidenciaResponse(
        UUID id,
        UUID viajeId,
        String viajeDescripcion,
        String pasajeroNombre,
        String conductorNombre,
        String vehiculoPatente,
        String tipo,
        String descripcion,
        EstadoIncidencia estado,
        String accionRealizada,
        String reportadoRol,
        LocalDateTime createdAt) {

    public static IncidenciaResponse desde(Incidencia i) {
        String viajeDesc = null;
        if (i.getViaje() != null) {
            viajeDesc = i.getViaje().getTipoTrayecto() + " " + i.getViaje().getJornadaTurno()
                    + (i.getViaje().getRuta() != null ? " " + i.getViaje().getRuta().getNombre() : "")
                    + " · " + i.getViaje().getFechaOperacion();
        }
        return new IncidenciaResponse(
                i.getId(),
                i.getViaje() != null ? i.getViaje().getId() : null,
                viajeDesc,
                i.getPasajero() != null ? i.getPasajero().getNombreCompleto() : null,
                i.getConductor() != null ? i.getConductor().getNombreCompleto() : null,
                i.getVehiculo() != null ? i.getVehiculo().getPatente() : null,
                i.getTipo(),
                i.getDescripcion(),
                i.getEstado(),
                i.getAccionRealizada(),
                i.getReportadoRol(),
                i.getCreatedAt());
    }
}
