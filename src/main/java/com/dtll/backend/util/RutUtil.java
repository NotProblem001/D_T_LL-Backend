package com.dtll.backend.util;

/** Normalización de RUT chileno para guardar y buscar siempre en el mismo formato. */
public final class RutUtil {

    private RutUtil() {
    }

    /** Formato canónico: sin puntos ni espacios, con guión, dígito verificador en mayúscula. Ej: 12345678-9. */
    public static String normalizar(String rut) {
        if (rut == null) {
            return null;
        }
        String limpio = rut.replace(".", "").replace(" ", "").toUpperCase();
        if (!limpio.contains("-") && limpio.length() > 1) {
            limpio = limpio.substring(0, limpio.length() - 1) + "-" + limpio.charAt(limpio.length() - 1);
        }
        return limpio;
    }
}
