package com.dtll.backend.security;

import com.dtll.backend.model.entity.Viaje;
import com.dtll.backend.repository.AsistenciaChecklistRepository;
import com.dtll.backend.repository.ViajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * SRS §3: "Permisos restringidos a su asignación".
 * Centraliza la validación de propiedad sobre un viaje:
 * - CONDUCTOR: solo viajes donde él es el conductor asignado.
 * - PASAJERO: solo viajes donde figura en el checklist.
 * - EMPRESA: solo viajes de su propio tenant (empresaId del JWT).
 * - ADMIN: acceso total.
 */
@Component
@RequiredArgsConstructor
public class ViajeAccessGuard {

    private final ViajeRepository viajeRepository;
    private final AsistenciaChecklistRepository asistenciaChecklistRepository;

    /** Solo el conductor asignado al viaje (o ADMIN) puede operar sobre él. */
    public Viaje exigirConductorDelViaje(UUID viajeId) {
        Viaje viaje = buscarViaje(viajeId);
        if (AuthenticatedUser.esAdmin()) {
            return viaje;
        }
        if (!"CONDUCTOR".equals(AuthenticatedUser.rol())
                || !viaje.getConductor().getId().equals(AuthenticatedUser.subjectId())) {
            throw new AccessDeniedException("No tienes asignado este viaje");
        }
        return viaje;
    }

    /** Lectura de tracking: conductor asignado, pasajero del checklist, empresa dueña o ADMIN. */
    public Viaje exigirLecturaViaje(UUID viajeId) {
        Viaje viaje = buscarViaje(viajeId);
        if (AuthenticatedUser.esAdmin()) {
            return viaje;
        }
        String rol = AuthenticatedUser.rol();
        boolean autorizado = switch (rol == null ? "" : rol) {
            case "CONDUCTOR" -> viaje.getConductor().getId().equals(AuthenticatedUser.subjectId());
            case "PASAJERO" -> {
                UUID pasajeroId = AuthenticatedUser.pasajeroIdONull();
                yield pasajeroId != null
                        && asistenciaChecklistRepository.existsByViajeIdAndPasajeroId(viajeId, pasajeroId);
            }
            case "EMPRESA" -> viaje.getEmpresaCliente().getId().equals(AuthenticatedUser.empresaId());
            default -> false;
        };
        if (!autorizado) {
            throw new AccessDeniedException("No tienes acceso a este viaje");
        }
        return viaje;
    }

    private Viaje buscarViaje(UUID viajeId) {
        return viajeRepository.findById(viajeId)
                .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado"));
    }
}
