package com.dtll.backend.dto.importacion;

import com.dtll.backend.model.enums.ResolucionRegistro;
import com.dtll.backend.model.enums.TipoMatch;
import com.dtll.backend.model.enums.UsoTransporte;

import java.util.List;
import java.util.UUID;

public record RegistroImportacionResponse(
        UUID id,
        String hojaOrigen,
        Integer filaOrigen,
        String nombreOriginal,
        String turno,
        String centroCosto,
        String cargo,
        String telefono,
        String direccion,
        String comuna,
        UsoTransporte usoTransporteDetectado,
        TipoMatch tipoMatch,
        ResolucionRegistro resolucion,
        UUID pasajeroId,
        String pasajeroNombre,
        List<CandidatoMatch> candidatos,
        String mensajeError) {
}
