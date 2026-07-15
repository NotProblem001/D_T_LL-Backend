package com.dtll.backend.service;

import com.dtll.backend.dto.tracking.TrackingResponse;
import com.dtll.backend.dto.tracking.TrackingUpdateRequest;
import com.dtll.backend.model.entity.Viaje;
import com.dtll.backend.model.entity.ViajeTracking;
import com.dtll.backend.repository.ViajeTrackingRepository;
import com.dtll.backend.security.ViajeAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final ViajeTrackingRepository viajeTrackingRepository;
    private final ViajeAccessGuard viajeAccessGuard;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public TrackingResponse actualizarUbicacion(UUID viajeId, TrackingUpdateRequest request) {
        // SRS §3: solo el conductor asignado (o ADMIN) puede emitir la ubicación GPS del viaje.
        Viaje viaje = viajeAccessGuard.exigirConductorDelViaje(viajeId);

        ViajeTracking tracking = viajeTrackingRepository.findById(viajeId)
                .orElseGet(() -> ViajeTracking.builder().viajeId(viajeId).viaje(viaje).build());

        tracking.setLatitud(request.lat());
        tracking.setLongitud(request.lng());
        tracking.setHeading(request.heading());
        tracking.setUpdatedAt(LocalDateTime.now());
        viajeTrackingRepository.save(tracking);

        TrackingResponse response = new TrackingResponse(
                viajeId, request.lat(), request.lng(), request.heading(), tracking.getUpdatedAt());

        messagingTemplate.convertAndSend("/topic/viaje/" + viajeId + "/ubicacion", response);

        return response;
    }

    public TrackingResponse obtenerUltimaUbicacion(UUID viajeId) {
        // SRS §3: pasajeros solo ven el vehículo de SU viaje; empresas, los de su tenant.
        viajeAccessGuard.exigirLecturaViaje(viajeId);
        ViajeTracking tracking = viajeTrackingRepository.findById(viajeId)
                .orElseThrow(() -> new IllegalArgumentException("Aún no hay ubicación registrada para este viaje"));

        return new TrackingResponse(viajeId, tracking.getLatitud(), tracking.getLongitud(),
                tracking.getHeading(), tracking.getUpdatedAt());
    }
}
