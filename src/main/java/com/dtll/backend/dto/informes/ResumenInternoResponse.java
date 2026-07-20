package com.dtll.backend.dto.informes;

import java.util.List;

/** Informes internos (sección 17): agregados por conductor, vehículo y pasajero en un rango. */
public record ResumenInternoResponse(
        List<FilaConductor> porConductor,
        List<FilaVehiculo> porVehiculo,
        List<FilaPasajero> porPasajero) {

    public record FilaConductor(String nombre, long recorridos, long recorridosFinalizados,
                                long pasajerosTransportados, long incidencias) {
    }

    public record FilaVehiculo(String patente, long recorridos, long pasajerosTransportados) {
    }

    public record FilaPasajero(String nombre, long asistencias, long ausencias, long cancelaciones) {
    }
}
