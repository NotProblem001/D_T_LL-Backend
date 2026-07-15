package com.dtll.backend.controller;

import com.dtll.backend.dto.tracking.TrackingResponse;
import com.dtll.backend.dto.tracking.TrackingUpdateRequest;
import com.dtll.backend.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/{viajeId}")
    public ResponseEntity<TrackingResponse> actualizar(@PathVariable UUID viajeId,
                                                         @RequestBody TrackingUpdateRequest request) {
        return ResponseEntity.ok(trackingService.actualizarUbicacion(viajeId, request));
    }

    @GetMapping("/{viajeId}")
    public ResponseEntity<TrackingResponse> obtener(@PathVariable UUID viajeId) {
        return ResponseEntity.ok(trackingService.obtenerUltimaUbicacion(viajeId));
    }
}
