package com.dtll.backend.service;

import com.dtll.backend.dto.checklist.ChecklistItemResponse;
import com.dtll.backend.dto.checklist.ChecklistUpdateRequest;
import com.dtll.backend.model.entity.AsistenciaChecklist;
import com.dtll.backend.model.entity.Pasajero;
import com.dtll.backend.model.entity.Viaje;
import com.dtll.backend.repository.AsistenciaChecklistRepository;
import com.dtll.backend.repository.PasajeroRepository;
import com.dtll.backend.security.ViajeAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final AsistenciaChecklistRepository asistenciaChecklistRepository;
    private final PasajeroRepository pasajeroRepository;
    private final ViajeAccessGuard viajeAccessGuard;
    private final NotificationService notificationService;

    public List<ChecklistItemResponse> obtenerPorViaje(UUID viajeId) {
        // SRS §3: el conductor solo ve el checklist de SU viaje asignado.
        viajeAccessGuard.exigirConductorDelViaje(viajeId);
        return listar(viajeId);
    }

    @Transactional
    public List<ChecklistItemResponse> actualizar(UUID viajeId, ChecklistUpdateRequest request) {
        viajeAccessGuard.exigirConductorDelViaje(viajeId);

        Map<UUID, AsistenciaChecklist> asistenciasPorId = asistenciaChecklistRepository.findByViajeId(viajeId).stream()
                .collect(Collectors.toMap(AsistenciaChecklist::getId, a -> a));

        for (ChecklistUpdateRequest.Item item : request.marcaciones()) {
            AsistenciaChecklist asistencia = asistenciasPorId.get(item.asistenciaId());
            if (asistencia == null) {
                continue; // no pertenece a este viaje, se ignora
            }
            asistencia.setEstado(item.estado());
            asistencia.setObservaciones(item.observaciones());
            asistencia.setHoraMarcaje(LocalDateTime.now());
        }

        asistenciaChecklistRepository.saveAll(asistenciasPorId.values());
        return listar(viajeId);
    }

    /**
     * SRS §2.1 Nóminas Dinámicas: agrega un pasajero a un viaje ya creado
     * sin necesidad de re-subir el Excel completo. Solo ADMIN (SecurityConfig).
     */
    @Transactional
    public List<ChecklistItemResponse> agregarPasajero(UUID viajeId, UUID pasajeroId) {
        Viaje viaje = viajeAccessGuard.exigirConductorDelViaje(viajeId); // ADMIN pasa directo

        Pasajero pasajero = pasajeroRepository.findById(pasajeroId)
                .orElseThrow(() -> new IllegalArgumentException("Pasajero no encontrado"));

        // Aislamiento B2B: el pasajero debe pertenecer a la misma empresa del viaje.
        if (!pasajero.getEmpresaCliente().getId().equals(viaje.getEmpresaCliente().getId())) {
            throw new IllegalArgumentException("El pasajero no pertenece a la empresa de este viaje");
        }
        if (asistenciaChecklistRepository.existsByViajeIdAndPasajeroId(viajeId, pasajeroId)) {
            throw new IllegalArgumentException("El pasajero ya está en el checklist de este viaje");
        }

        asistenciaChecklistRepository.save(AsistenciaChecklist.builder()
                .viaje(viaje)
                .pasajero(pasajero)
                .build());

        // SRS §2.4: notificación automática de recogida (best-effort, async).
        notificationService.notificarAsignacionPasajero(pasajero, viaje);

        return listar(viajeId);
    }

    /** SRS §2.1: descarta un pasajero del viaje desde la interfaz. */
    @Transactional
    public List<ChecklistItemResponse> quitarPasajero(UUID viajeId, UUID asistenciaId) {
        viajeAccessGuard.exigirConductorDelViaje(viajeId);
        AsistenciaChecklist asistencia = asistenciaChecklistRepository.findByIdAndViajeId(asistenciaId, viajeId)
                .orElseThrow(() -> new IllegalArgumentException("La asistencia no pertenece a este viaje"));
        asistenciaChecklistRepository.delete(asistencia);
        return listar(viajeId);
    }

    private List<ChecklistItemResponse> listar(UUID viajeId) {
        return asistenciaChecklistRepository.findByViajeId(viajeId).stream()
                .map(a -> new ChecklistItemResponse(
                        a.getId(),
                        a.getPasajero().getId(),
                        a.getPasajero().getNombreCompleto(),
                        a.getPasajero().getPuntoParadaAsignado(),
                        a.getEstado()))
                .toList();
    }
}
