package com.dtll.backend.dto.mensajeria;

import java.util.List;

/** Todo lo necesario para comunicar un recorrido: plantilla, grupo, teléfonos e historial. */
public record MensajeriaViajeResponse(
        String textoSugerido,
        String grupoWhatsapp,
        List<TelefonoPasajeroResponse> telefonos,
        List<String> pasajerosSinTelefono,
        List<MensajeRutaResponse> mensajes) {
}
