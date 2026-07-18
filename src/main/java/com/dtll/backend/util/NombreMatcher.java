package com.dtll.backend.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Comparación difusa de nombres ya normalizados (ver {@link Normalizador#nombre}).
 * Cubre los casos medidos en los archivos reales: apellidos antes del nombre
 * ("RUIZ ARCE LUIS" = "LUIS RUIZ ARCE"), typos de una letra ("ISMAEL"/"ISRAEL")
 * y nombres parciales ("MARTIN" ⊂ "MARTIN VALENCIA").
 */
public final class NombreMatcher {

    /** Sobre este puntaje se ofrece el candidato como sugerencia al operador. */
    public static final double UMBRAL_SUGERENCIA = 0.82;

    private NombreMatcher() {
    }

    /** Mismas palabras en cualquier orden. */
    public static boolean mismosTokens(String a, String b) {
        return tokens(a).equals(tokens(b));
    }

    /**
     * Puntaje 0..1 combinando Jaro-Winkler del texto completo y el mejor
     * apareo palabra a palabra (tolera orden distinto y palabras faltantes).
     */
    public static double similitud(String a, String b) {
        if (a == null || b == null || a.isBlank() || b.isBlank()) {
            return 0;
        }
        // Con varias palabras manda el apareo palabra a palabra: el Jaro-Winkler
        // del texto completo se infla cuando solo coincide el nombre de pila
        // ("JORGE ARREDONDO" vs "JORGE BURGOS").
        if (a.contains(" ") && b.contains(" ")) {
            return similitudPorTokens(a, b);
        }
        return Math.max(jaroWinkler(a, b), similitudPorTokens(a, b));
    }

    private static double similitudPorTokens(String a, String b) {
        Set<String> ta = tokens(a);
        Set<String> tb = tokens(b);
        if (ta.isEmpty() || tb.isEmpty()) {
            return 0;
        }
        Set<String> cortos = ta.size() <= tb.size() ? ta : tb;
        Set<String> largos = ta.size() <= tb.size() ? tb : ta;
        double suma = 0;
        for (String token : cortos) {
            double mejor = 0;
            for (String otro : largos) {
                mejor = Math.max(mejor, jaroWinkler(token, otro));
            }
            suma += mejor;
        }
        double promedio = suma / cortos.size();
        // Penaliza levemente cuando faltan palabras (nombre parcial).
        double cobertura = (double) cortos.size() / largos.size();
        return promedio * (0.85 + 0.15 * cobertura);
    }

    private static Set<String> tokens(String s) {
        Set<String> set = new HashSet<>();
        if (s != null) {
            set.addAll(Arrays.asList(s.trim().split("\\s+")));
            set.remove("");
        }
        return set;
    }

    // ------------------------------------------------------------------ Jaro-Winkler

    static double jaroWinkler(String a, String b) {
        double jaro = jaro(a, b);
        // Bonus por prefijo común (máx 4 caracteres), estándar de Winkler.
        int prefijo = 0;
        for (int i = 0; i < Math.min(4, Math.min(a.length(), b.length())); i++) {
            if (a.charAt(i) == b.charAt(i)) prefijo++;
            else break;
        }
        return jaro + prefijo * 0.1 * (1 - jaro);
    }

    private static double jaro(String a, String b) {
        if (a.equals(b)) {
            return 1;
        }
        int la = a.length();
        int lb = b.length();
        if (la == 0 || lb == 0) {
            return 0;
        }
        int ventana = Math.max(la, lb) / 2 - 1;
        boolean[] ma = new boolean[la];
        boolean[] mb = new boolean[lb];

        int coincidencias = 0;
        for (int i = 0; i < la; i++) {
            int desde = Math.max(0, i - ventana);
            int hasta = Math.min(lb - 1, i + ventana);
            for (int j = desde; j <= hasta; j++) {
                if (!mb[j] && a.charAt(i) == b.charAt(j)) {
                    ma[i] = true;
                    mb[j] = true;
                    coincidencias++;
                    break;
                }
            }
        }
        if (coincidencias == 0) {
            return 0;
        }

        int transposiciones = 0;
        int k = 0;
        for (int i = 0; i < la; i++) {
            if (ma[i]) {
                while (!mb[k]) k++;
                if (a.charAt(i) != b.charAt(k)) transposiciones++;
                k++;
            }
        }
        double m = coincidencias;
        return (m / la + m / lb + (m - transposiciones / 2.0) / m) / 3.0;
    }
}
