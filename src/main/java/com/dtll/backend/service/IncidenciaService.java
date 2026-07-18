package com.dtll.backend.service;

import com.dtll.backend.dto.incidencia.IncidenciaRequest;
import com.dtll.backend.dto.incidencia.IncidenciaResponse;
import com.dtll.backend.dto.incidencia.IncidenciaUpdateRequest;
import com.dtll.backend.model.entity.Incidencia;
import com.dtll.backend.model.entity.Viaje;
import com.dtll.backend.model.enums.EstadoIncidencia;
import com.dtll.backend.repository.*;
import com.dtll.backend.security.AuthenticatedUser;
import com.dtll.backend.security.ViajeAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Incidencias del recorrido (sección 11): las reporta el conductor u operación y se gestionan a cierre. */
@Service
@RequiredArgsConstructor
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final ViajeRepository viajeRepository;
    private final PasajeroRepository pasajeroRepository;
    private final ConductorRepository conductorRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ViajeAccessGuard viajeAccessGuard;

    @Transactional
    public IncidenciaResponse crear(IncidenciaRequest request) {
        if (request.tipo() == null || request.tipo().isBlank()) {
            throw new IllegalArgumentException("El tipo de incidencia es obligatorio");
        }
        if (request.descripcion() == null || request.descripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }

        boolean esConductor = "CONDUCTOR".equals(AuthenticatedUser.rol());
        Viaje viaje = null;
        if (request.viajeId() != null) {
            // Un conductor solo reporta sobre sus propios viajes.
            viaje = esConductor
                    ? viajeAccessGuard.exigirConductorDelViaje(request.viajeId())
                    : viajeRepository.findById(request.viajeId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Viaje no encontrado: " + request.viajeId()));
        }

        Incidencia.IncidenciaBuilder b = Incidencia.builder()
                .viaje(viaje)
                .tipo(request.tipo().trim())
                .descripcion(request.descripcion().trim())
                .reportadoPor(AuthenticatedUser.subjectId())
                .reportadoRol(AuthenticatedUser.rol());

        if (request.pasajeroId() != null) {
            b.pasajero(pasajeroRepository.findById(request.pasajeroId())
                    .orElseThrow(() -> new IllegalArgumentException("Pasajero no encontrado")));
        }
        // El conductor que reporta queda asociado automáticamente.
        UUID conductorId = esConductor ? AuthenticatedUser.subjectId() : request.conductorId();
        if (conductorId == null && viaje != null && viaje.getConductor() != null) {
            conductorId = viaje.getConductor().getId();
        }
        if (conductorId != null) {
            b.conductor(conductorRepository.findById(conductorId)
                    .orElseThrow(() -> new IllegalArgumentException("Conductor no encontrado")));
        }
        UUID vehiculoId = request.vehiculoId();
        if (vehiculoId == null && viaje != null && viaje.getVehiculo() != null) {
            vehiculoId = viaje.getVehiculo().getId();
        }
        if (vehiculoId != null) {
            b.vehiculo(vehiculoRepository.findById(vehiculoId)
                    .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado")));
        }

        return IncidenciaResponse.desde(incidenciaRepository.save(b.build()));
    }

    @Transactional(readOnly = true)
    public List<IncidenciaResponse> listar(EstadoIncidencia estado, UUID viajeId) {
        List<Incidencia> incidencias;
        if (viajeId != null) {
            incidencias = incidenciaRepository.findByViajeIdOrderByCreatedAtDesc(viajeId);
        } else if (estado != null) {
            incidencias = incidenciaRepository.findTop200ByEstadoOrderByCreatedAtDesc(estado);
        } else {
            incidencias = incidenciaRepository.findTop200ByOrderByCreatedAtDesc();
        }
        return incidencias.stream().map(IncidenciaResponse::desde).toList();
    }

    /** Gestión (solo ADMIN/OPERADOR, controlado en el controller): estado y acción realizada. */
    @Transactional
    public IncidenciaResponse actualizar(UUID id, IncidenciaUpdateRequest request) {
        Incidencia incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incidencia no encontrada: " + id));
        if (request.estado() != null) {
            incidencia.setEstado(request.estado());
        }
        if (request.accionRealizada() != null && !request.accionRealizada().isBlank()) {
            incidencia.setAccionRealizada(request.accionRealizada().trim());
        }
        return IncidenciaResponse.desde(incidenciaRepository.save(incidencia));
    }
}
