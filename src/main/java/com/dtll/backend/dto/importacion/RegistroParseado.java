package com.dtll.backend.dto.importacion;

/**
 * Fila cruda extraída de un archivo/texto de turnos, antes de limpiar y matchear.
 * error != null marca valores que ocupaban una celda de nombre sin serlo ("12 HORAS").
 */
public record RegistroParseado(
        String nombreOriginal,
        String nombreNormalizado,
        String turno,
        String centroCosto,
        String cargo,
        String telefono,
        String direccion,
        String comuna,
        String hoja,
        Integer fila,
        String error) {

    /** Registro de nómina matriz/texto: solo nombre + turno + contexto. */
    public static RegistroParseado simple(String nombreOriginal, String nombreNormalizado,
                                          String turno, String centroCosto, String cargo) {
        return new RegistroParseado(nombreOriginal, nombreNormalizado, turno, centroCosto, cargo,
                null, null, null, null, null, null);
    }

    public static RegistroParseado conError(String valorOriginal, String turno,
                                            String hoja, Integer fila, String error) {
        return new RegistroParseado(valorOriginal, null, turno, null, null,
                null, null, null, hoja, fila, error);
    }
}
