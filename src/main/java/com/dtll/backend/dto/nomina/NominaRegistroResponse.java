package com.dtll.backend.dto.nomina;

import com.dtll.backend.model.entity.NominaTurno;
import com.dtll.backend.model.entity.Pasajero;

import java.util.UUID;

/** Fila de la vista de planilla/nómina del Admin: turno + datos de contacto del pasajero. */
public record NominaRegistroResponse(
        UUID id,
        int anio,
        int semana,
        String turno,
        String nombre,
        String telefono,
        String direccion,
        String comuna,
        String centroCosto,
        String cargo,
        boolean datosCompletos,
        UUID pasajeroId) {

    public static NominaRegistroResponse desde(NominaTurno n) {
        Pasajero p = n.getPasajero();
        String telefono = p != null ? p.getTelefono() : null;
        String direccion = p != null ? p.getDireccionReferencia() : null;
        String comuna = p != null ? p.getComuna() : null;
        boolean completos = direccion != null && !direccion.isBlank();
        return new NominaRegistroResponse(
                n.getId(),
                n.getAnio(),
                n.getSemana(),
                n.getTurno(),
                p != null ? p.getNombreCompleto() : n.getNombreOriginal(),
                telefono,
                direccion,
                comuna,
                n.getCentroCosto(),
                n.getCargo(),
                completos,
                p != null ? p.getId() : null);
    }
}
