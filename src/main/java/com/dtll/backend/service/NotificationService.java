package com.dtll.backend.service;

import com.dtll.backend.model.entity.Conductor;
import com.dtll.backend.model.entity.Pasajero;
import com.dtll.backend.model.entity.Viaje;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * SRS §2.4: Notificaciones automáticas vía Gmail/SMTP.
 * Best-effort y asíncrono: un fallo de correo jamás debe romper
 * la operación de negocio que lo disparó (import, asignación, etc.).
 * Con notificaciones.habilitadas=false solo se loguea (útil en dev).
 */
@Service
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final boolean habilitadas;
    private final String remitente;

    public NotificationService(JavaMailSender mailSender,
                                @Value("${notificaciones.habilitadas}") boolean habilitadas,
                                @Value("${notificaciones.remitente}") String remitente) {
        this.mailSender = mailSender;
        this.habilitadas = habilitadas;
        this.remitente = remitente;
    }

    /** Aviso al pasajero de que fue asignado a un transporte. */
    @Async
    public void notificarAsignacionPasajero(Pasajero pasajero, Viaje viaje) {
        if (pasajero.getEmail() == null || pasajero.getEmail().isBlank()) {
            log.info("Pasajero {} sin email registrado; se omite notificación", pasajero.getId());
            return;
        }
        enviar(pasajero.getEmail(),
                "DTLL: Transporte asignado para el " + viaje.getFechaOperacion(),
                """
                Hola %s,

                Se te asignó transporte para el día %s (%s, %s).
                Punto de recogida: %s

                Podrás seguir tu vehículo en tiempo real desde la plataforma.

                — Donde Te Llevo
                """.formatted(pasajero.getNombreCompleto(), viaje.getFechaOperacion(),
                        valorODesconocido(viaje.getJornadaTurno()), valorODesconocido(viaje.getTipoTrayecto()),
                        valorODesconocido(pasajero.getPuntoParadaAsignado())));
    }

    /** Envío de asignación de ruta al conductor (requiere email si se registra en el futuro; hoy loguea a falta de campo). */
    @Async
    public void notificarAsignacionConductor(Conductor conductor, Viaje viaje) {
        // El modelo actual de Conductor no tiene email; se deja el hook listo.
        log.info("Asignación de viaje {} (código {}) al conductor {} — canal email pendiente de campo",
                viaje.getId(), viaje.getCodigoRutaLogin(), conductor.getNombreCompleto());
    }

    /** Mensaje libre (cambios de horario, soporte). */
    @Async
    public void notificar(String destinatario, String asunto, String cuerpo) {
        enviar(destinatario, asunto, cuerpo);
    }

    private void enviar(String destinatario, String asunto, String cuerpo) {
        if (!habilitadas) {
            log.info("[Notificaciones deshabilitadas] Para: {} | Asunto: {}", destinatario, asunto);
            return;
        }
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
            log.info("Notificación enviada a {}", destinatario);
        } catch (Exception e) {
            // Best-effort: se registra pero no se propaga.
            log.error("Fallo al enviar notificación a {}: {}", destinatario, e.getMessage());
        }
    }

    private String valorODesconocido(String valor) {
        return (valor == null || valor.isBlank()) ? "por confirmar" : valor;
    }
}
