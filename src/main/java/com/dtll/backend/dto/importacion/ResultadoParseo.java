package com.dtll.backend.dto.importacion;

import java.util.List;

/** Salida de un parser de nómina: filas crudas + semana detectada en el contenido (si la hay). */
public record ResultadoParseo(List<RegistroParseado> registros, Integer semanaDetectada) {
}
