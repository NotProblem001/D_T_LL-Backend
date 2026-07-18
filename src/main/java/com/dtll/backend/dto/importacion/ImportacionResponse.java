package com.dtll.backend.dto.importacion;

import com.dtll.backend.model.entity.Importacion;
import com.dtll.backend.model.enums.EstadoImportacion;
import com.dtll.backend.model.enums.TipoImportacion;

import java.time.LocalDateTime;
import java.util.UUID;

/** Resumen de una importación en staging (la sección 22 del requerimiento). */
public record ImportacionResponse(
        UUID id,
        TipoImportacion tipo,
        String nombreArchivo,
        Integer anio,
        Integer semana,
        EstadoImportacion estado,
        Integer totalRegistros,
        Integer totalEncontrados,
        Integer totalSugerencias,
        Integer totalNuevos,
        Integer totalDuplicados,
        Integer totalErrores,
        LocalDateTime createdAt,
        LocalDateTime confirmadaAt) {

    public static ImportacionResponse desde(Importacion i) {
        return new ImportacionResponse(
                i.getId(),
                i.getTipo(),
                i.getNombreArchivo(),
                i.getAnio(),
                i.getSemana(),
                i.getEstado(),
                i.getTotalRegistros(),
                i.getTotalEncontrados(),
                i.getTotalSugerencias(),
                i.getTotalNuevos(),
                i.getTotalDuplicados(),
                i.getTotalErrores(),
                i.getCreatedAt(),
                i.getConfirmadaAt());
    }
}
