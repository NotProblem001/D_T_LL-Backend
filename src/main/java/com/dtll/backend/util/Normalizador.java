package com.dtll.backend.util;

import java.text.Normalizer;
import java.util.Locale;

/** Normalización de nombres para deduplicar pasajeros y cruzar nóminas. */
public final class Normalizador {

    private Normalizador() {
    }

    /** MAYÚSCULAS, sin tildes, sin caracteres invisibles, espacios colapsados. */
    public static String nombre(String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")            // tildes/diacríticos
                .replaceAll("[\\u200B-\\u200F\\uFEFF\\u00A0]", " ") // invisibles/nbsp
                .replaceAll("[^\\p{L}\\p{N} .'-]", " ")
                .trim()
                .replaceAll("\\s+", " ");
        return limpio.toUpperCase(Locale.ROOT);
    }
}
