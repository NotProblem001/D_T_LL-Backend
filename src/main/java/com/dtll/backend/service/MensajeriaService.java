package com.dtll.backend.service;

import com.dtll.backend.dto.mensajeria.*;
import com.dtll.backend.model.entity.AsistenciaChecklist;
import com.dtll.backend.model.entity.MensajeRuta;
import com.dtll.backend.model.entity.Pasajero;
import com.dtll.backend.model.entity.Viaje;
import com.dtll.backend.repository.AsistenciaChecklistRepository;
import com.dtll.backend.repository.MensajeRutaRepository;
import com.dtll.backend.repository.ViajeRepository;
import com.dtll.backend.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Etapa 5 (sección 10): genera el mensaje tipo para el grupo de WhatsApp de un
 * recorrido, entrega los teléfonos para enlaces wa.me y registra el envío.
 * La integración con WhatsApp Business API queda como evolución futura.
 */
@Service
@RequiredArgsConstructor
public class MensajeriaService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ViajeRepository viajeRepository;
    private final AsistenciaChecklistRepository asistenciaRepository;
    private final MensajeRutaRepository mensajeRepository;

    @Transactional(readOnly = true)
    public MensajeriaViajeResponse obtener(UUID viajeId) {
        Viaje viaje = obtenerViaje(viajeId);

        List<TelefonoPasajeroResponse> telefonos = new ArrayList<>();
        List<String> sinTelefono = new ArrayList<>();
        for (AsistenciaChecklist a : asistenciaRepository.findByViajeId(viajeId)) {
            Pasajero p = a.getPasajero();
            if (p.getTelefono() != null && !p.getTelefono().isBlank()) {
                telefonos.add(new TelefonoPasajeroResponse(p.getId(), p.getNombreCompleto(),
                        soloDigitos(p.getTelefono())));
            } else {
                sinTelefono.add(p.getNombreCompleto());
            }
        }

        return new MensajeriaViajeResponse(
                generarTexto(viaje),
                viaje.getRuta() != null ? viaje.getRuta().getGrupoWhatsapp() : null,
                telefonos,
                sinTelefono,
                mensajeRepository.findByViajeIdOrderByCreatedAtDesc(viajeId).stream()
                        .map(MensajeRutaResponse::desde)
                        .toList());
    }

    @Transactional
    public MensajeRutaResponse guardar(UUID viajeId, GuardarMensajeRequest request) {
        Viaje viaje = obtenerViaje(viajeId);
        if (request.texto() == null || request.texto().isBlank()) {
            throw new IllegalArgumentException("El texto del mensaje es obligatorio");
        }

        MensajeRuta mensaje = MensajeRuta.builder()
                .viaje(viaje)
                .texto(request.texto().trim())
                .grupoWhatsapp(limpiar(request.grupoWhatsapp()))
                .build();
        if (Boolean.TRUE.equals(request.enviado())) {
            marcarEnviadoInterno(mensaje);
        }

        // El grupo indicado queda registrado en la ruta para la próxima vez.
        if (mensaje.getGrupoWhatsapp() != null && viaje.getRuta() != null) {
            viaje.getRuta().setGrupoWhatsapp(mensaje.getGrupoWhatsapp());
        }

        return MensajeRutaResponse.desde(mensajeRepository.save(mensaje));
    }

    @Transactional
    public MensajeRutaResponse marcarEnviado(UUID mensajeId) {
        MensajeRuta mensaje = mensajeRepository.findById(mensajeId)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado: " + mensajeId));
        marcarEnviadoInterno(mensaje);
        return MensajeRutaResponse.desde(mensajeRepository.save(mensaje));
    }

    // ------------------------------------------------------------------ helpers

    /** Plantilla de la sección 10, con saludo según jornada y datos del viaje. */
    private String generarTexto(Viaje v) {
        String saludo = switch (v.getJornadaTurno() == null ? "" : v.getJornadaTurno()) {
            case "TARDE" -> "Buenas tardes";
            case "NOCHE" -> "Buenas noches";
            default -> "Buenos días";
        };
        String nombreRuta = (tipoLegible(v.getTipoTrayecto()) + " " + jornadaLegible(v.getJornadaTurno())
                + (v.getRuta() != null ? " " + v.getRuta().getNombre() : "")).trim();
        String hora = v.getHoraProgramadaInicio() != null
                ? v.getHoraProgramadaInicio().format(HORA) + " horas" : "por confirmar";
        String conductor = v.getConductor() != null
                ? v.getConductor().getNombreCompleto() : "por confirmar";
        String vehiculo = v.getVehiculo() != null
                ? ((v.getVehiculo().getTipoVehiculo() != null ? v.getVehiculo().getTipoVehiculo() : "Vehículo")
                        + " patente " + v.getVehiculo().getPatente())
                : "por confirmar";

        return saludo + ".\n"
                + "La ruta " + nombreRuta + " del " + v.getFechaOperacion().format(FECHA)
                + " iniciará a las " + hora + ".\n"
                + "Conductor asignado: " + conductor + ".\n"
                + "Vehículo: " + vehiculo + ".\n"
                + "Por favor, estén atentos al grupo. Ante cualquier inconveniente, "
                + "informen oportunamente por este medio.";
    }

    private void marcarEnviadoInterno(MensajeRuta mensaje) {
        mensaje.setEnviado(true);
        mensaje.setEnviadoAt(LocalDateTime.now());
        mensaje.setEnviadoPor(AuthenticatedUser.subjectId());
    }

    private Viaje obtenerViaje(UUID viajeId) {
        return viajeRepository.findById(viajeId)
                .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado: " + viajeId));
    }

    private String tipoLegible(String tipo) {
        if (tipo == null) return "";
        return switch (tipo) {
            case "ENTRADA" -> "Entrada";
            case "SALIDA" -> "Salida";
            default -> tipo;
        };
    }

    private String jornadaLegible(String jornada) {
        if (jornada == null) return "";
        return switch (jornada) {
            case "MANANA" -> "Mañana";
            case "TARDE" -> "Tarde";
            case "NOCHE" -> "Noche";
            default -> jornada;
        };
    }

    private String soloDigitos(String telefono) {
        return telefono.replaceAll("\\D", "");
    }

    private String limpiar(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
