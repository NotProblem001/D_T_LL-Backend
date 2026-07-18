package com.dtll.backend.util;

import com.dtll.backend.model.enums.UsoTransporte;

import java.util.regex.Pattern;

/**
 * Limpieza de los campos sucios medidos en los archivos reales: teléfonos con y
 * sin prefijo 56, "SIN DATO"/"No utiliza" escritos en cualquier columna,
 * caracteres invisibles de WhatsApp y comunas dobles.
 */
public final class LimpiadorCampos {

    private static final Pattern NO_USA = Pattern.compile("(?i)no\\s+(utiliza|usa|lo\\s+utiliza)");
    private static final Pattern OCASIONAL = Pattern.compile("(?i)ocasional");

    private LimpiadorCampos() {
    }

    /**
     * Teléfono chileno normalizado a formato 569XXXXXXXX, o null si no hay dato
     * válido (vacío, "0", "SIN DATO", "No utiliza", texto).
     */
    public static String telefono(String valor) {
        if (valor == null) {
            return null;
        }
        String digitos = valor.replaceAll("\\D", "");
        if (digitos.isEmpty() || digitos.matches("0+")) {
            return null;
        }
        if (digitos.length() == 9 && digitos.startsWith("9")) {
            return "56" + digitos;
        }
        if (digitos.length() == 11 && digitos.startsWith("56")) {
            return digitos;
        }
        // Largo inesperado: se conserva tal cual para que el operador lo revise.
        return digitos;
    }

    /**
     * Detecta anotaciones de uso del servicio escritas en cualquier campo
     * (teléfono, dirección, comuna). null si el texto no dice nada al respecto.
     */
    public static UsoTransporte usoDetectado(String... valores) {
        for (String v : valores) {
            if (v == null) continue;
            if (NO_USA.matcher(v).find()) {
                return UsoTransporte.NO;
            }
            if (OCASIONAL.matcher(v).find()) {
                return UsoTransporte.OCASIONAL;
            }
        }
        return null;
    }

    /**
     * Dirección sin la anotación de no-uso: si la celda es solo la anotación
     * devuelve null; si la anotación viene embebida (entre paréntesis) la quita.
     */
    public static String direccion(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor
                .replaceAll("(?i)\\(\\s*no\\s+utiliza[^)]*\\)?", "")
                .trim();
        String norm = Normalizador.nombre(limpio);
        if (norm.isBlank() || norm.equals("0") || NO_USA.matcher(norm).find()
                || norm.equals("SIN DATO")) {
            return null;
        }
        return limpio;
    }

    /** Comuna limpia, o null. Comunas dobles ("Lampa/Santiago") se conservan para revisión. */
    public static String comuna(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        String norm = Normalizador.nombre(limpio);
        if (norm.isBlank() || norm.equals("0") || norm.equals("SIN DATO") || NO_USA.matcher(norm).find()) {
            return null;
        }
        return limpio;
    }
}
