package com.dtll.backend.dto.importacion;

import java.util.List;
import java.util.Map;

/** Resumen de una importación de BDD de pasajeros o de nómina semanal. */
public record NominaImportResponse(
        String tipo,                    // BDD | NOMINA
        Integer anio,
        Integer semana,
        Map<String, Integer> porTurno,  // MANANA/TARDE/NOCHE -> cantidad (solo NOMINA)
        Integer pasajerosCreados,       // solo BDD
        Integer pasajerosActualizados,  // solo BDD
        Integer sinCambios,             // solo BDD
        Integer conDatos,               // solo NOMINA: pasajeros con dirección/teléfono
        Integer sinDatos,               // solo NOMINA: pasajeros a completar
        List<String> pendientes         // nombres sin datos de contacto
) {

    public static NominaImportResponse deBdd(int creados, int actualizados, int sinCambios,
                                             List<String> observaciones) {
        return new NominaImportResponse("BDD", null, null, null,
                creados, actualizados, sinCambios, null, null, observaciones);
    }

    public static NominaImportResponse deNomina(int anio, int semana, Map<String, Integer> porTurno,
                                                int conDatos, int sinDatos, List<String> pendientes) {
        return new NominaImportResponse("NOMINA", anio, semana, porTurno,
                null, null, null, conDatos, sinDatos, pendientes);
    }
}
