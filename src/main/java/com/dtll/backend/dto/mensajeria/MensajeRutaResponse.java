package com.dtll.backend.dto.mensajeria;

import com.dtll.backend.model.entity.MensajeRuta;

import java.time.LocalDateTime;
import java.util.UUID;

public record MensajeRutaResponse(
        UUID id,
        String texto,
        String grupoWhatsapp,
        Boolean enviado,
        LocalDateTime enviadoAt,
        LocalDateTime createdAt) {

    public static MensajeRutaResponse desde(MensajeRuta m) {
        return new MensajeRutaResponse(
                m.getId(),
                m.getTexto(),
                m.getGrupoWhatsapp(),
                m.getEnviado(),
                m.getEnviadoAt(),
                m.getCreatedAt());
    }
}
