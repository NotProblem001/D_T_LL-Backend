package com.dtll.backend.dto.mensajeria;

/**
 * Registra el mensaje de un recorrido. enviado=true lo marca como enviado con
 * fecha/hora y usuario; el grupo indicado queda además guardado en la ruta.
 */
public record GuardarMensajeRequest(String texto, String grupoWhatsapp, Boolean enviado) {
}
