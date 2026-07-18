package com.dtll.backend.service;

import com.dtll.backend.dto.conductor.ViajeConductorResponse;
import com.dtll.backend.dto.maestros.EstadoAsistenciaResponse;
import com.dtll.backend.model.entity.AsistenciaChecklist;
import com.dtll.backend.model.entity.Viaje;
import com.dtll.backend.model.enums.EstadoViaje;
import com.dtll.backend.repository.AsistenciaChecklistRepository;
import com.dtll.backend.repository.ViajeRepository;
import com.dtll.backend.security.AuthenticatedUser;
import com.dtll.backend.security.ViajeAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Etapa 4: ciclo operativo del conductor desde su teléfono — mis recorridos,
 * confirmar la asignación, iniciar (hora real), finalizar (hora real).
 */
@Service
@RequiredArgsConstructor
public class ConductorViajeService {

    private final ViajeRepository viajeRepository;
    private final AsistenciaChecklistRepository asistenciaRepository;
    private final ViajeAccessGuard viajeAccessGuard;
    private final ConfiguracionService configuracionService;

    /** Recorridos del conductor autenticado para una fecha (por defecto hoy). */
    @Transactional(readOnly = true)
    public List<ViajeConductorResponse> misViajes(LocalDate fecha) {
        UUID conductorId = AuthenticatedUser.subjectId();
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        return viajeRepository.findByConductorIdAndFechaOperacion(conductorId, dia).stream()
                .filter(v -> v.getEstado() != EstadoViaje.CANCELADO)
                .sorted((a, b) -> {
                    if (a.getHoraProgramadaInicio() == null) return 1;
                    if (b.getHoraProgramadaInicio() == null) return -1;
                    return a.getHoraProgramadaInicio().compareTo(b.getHoraProgramadaInicio());
                })
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ViajeConductorResponse detalle(UUID viajeId) {
        return aResponse(viajeAccessGuard.exigirConductorDelViaje(viajeId));
    }

    /** El conductor acepta la asignación recibida. */
    @Transactional
    public ViajeConductorResponse confirmar(UUID viajeId) {
        Viaje viaje = viajeAccessGuard.exigirConductorDelViaje(viajeId);
        exigirEstado(viaje, "confirmar", EstadoViaje.ASIGNADO, EstadoViaje.PROGRAMADO, EstadoViaje.REPROGRAMADO);
        viaje.setEstado(EstadoViaje.CONFIRMADO);
        return aResponse(viajeRepository.save(viaje));
    }

    /** Inicio del recorrido: registra la hora real. */
    @Transactional
    public ViajeConductorResponse iniciar(UUID viajeId) {
        Viaje viaje = viajeAccessGuard.exigirConductorDelViaje(viajeId);
        exigirEstado(viaje, "iniciar", EstadoViaje.CONFIRMADO, EstadoViaje.ASIGNADO,
                EstadoViaje.PROGRAMADO, EstadoViaje.REPROGRAMADO);
        viaje.setEstado(EstadoViaje.EN_CURSO);
        viaje.setHoraRealInicio(LocalDateTime.now());
        return aResponse(viajeRepository.save(viaje));
    }

    /** Término del recorrido: exige el checklist completo y registra la hora real. */
    @Transactional
    public ViajeConductorResponse finalizar(UUID viajeId) {
        Viaje viaje = viajeAccessGuard.exigirConductorDelViaje(viajeId);
        exigirEstado(viaje, "finalizar", EstadoViaje.EN_CURSO);
        long pendientes = contarPendientes(viajeId);
        if (pendientes > 0) {
            throw new IllegalStateException("Quedan " + pendientes
                    + " pasajero(s) sin marcar asistencia. Completa el checklist antes de finalizar.");
        }
        viaje.setEstado(EstadoViaje.FINALIZADO);
        viaje.setHoraRealTermino(LocalDateTime.now());
        return aResponse(viajeRepository.save(viaje));
    }

    /** Estados de asistencia activos del maestro, para los botones del checklist. */
    @Transactional(readOnly = true)
    public List<EstadoAsistenciaResponse> estadosAsistencia() {
        return configuracionService.listarEstadosAsistencia().stream()
                .filter(EstadoAsistenciaResponse::activo)
                .toList();
    }

    private void exigirEstado(Viaje viaje, String accion, EstadoViaje... permitidos) {
        for (EstadoViaje e : permitidos) {
            if (viaje.getEstado() == e) {
                return;
            }
        }
        throw new IllegalStateException("No se puede " + accion + " un recorrido en estado " + viaje.getEstado());
    }

    private long contarPendientes(UUID viajeId) {
        return asistenciaRepository.findByViajeId(viajeId).stream()
                .filter(a -> AsistenciaChecklist.ESTADO_PENDIENTE.equals(a.getEstado()))
                .count();
    }

    private ViajeConductorResponse aResponse(Viaje v) {
        return ViajeConductorResponse.desde(v,
                asistenciaRepository.countByViajeId(v.getId()),
                contarPendientes(v.getId()));
    }
}
