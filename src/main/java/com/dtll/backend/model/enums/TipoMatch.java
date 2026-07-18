package com.dtll.backend.model.enums;

/** Resultado del cruce de un registro importado contra la BDD de pasajeros. */
public enum TipoMatch {
    EXACTO,     // nombre normalizado idéntico
    TOKENS,     // mismas palabras en otro orden (RUIZ ARCE LUIS = LUIS RUIZ ARCE)
    SUGERENCIA, // parecido (typos, abreviaciones) — requiere confirmación del operador
    NUEVO,      // sin candidato: propuesta de pasajero nuevo
    DUPLICADO,  // repetido dentro del mismo archivo
    ERROR       // valor que no parece un nombre ("12 HORAS", etc.)
}
