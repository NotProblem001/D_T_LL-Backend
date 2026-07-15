package com.dtll.backend.controller;

import com.dtll.backend.dto.checklist.AgregarPasajeroRequest;
import com.dtll.backend.dto.checklist.ChecklistItemResponse;
import com.dtll.backend.dto.checklist.ChecklistUpdateRequest;
import com.dtll.backend.service.ChecklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/checklist")
@RequiredArgsConstructor
public class ChecklistController {

    private final ChecklistService checklistService;

    @GetMapping("/{viajeId}")
    public ResponseEntity<List<ChecklistItemResponse>> obtener(@PathVariable UUID viajeId) {
        return ResponseEntity.ok(checklistService.obtenerPorViaje(viajeId));
    }

    @PutMapping("/{viajeId}")
    public ResponseEntity<List<ChecklistItemResponse>> actualizar(@PathVariable UUID viajeId,
                                                                    @RequestBody ChecklistUpdateRequest request) {
        return ResponseEntity.ok(checklistService.actualizar(viajeId, request));
    }

    /** SRS §2.1 Nóminas dinámicas: agregar pasajero sin re-subir el Excel. Solo ADMIN. */
    @PostMapping("/{viajeId}/pasajeros")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChecklistItemResponse>> agregarPasajero(@PathVariable UUID viajeId,
                                                                         @RequestBody AgregarPasajeroRequest request) {
        return ResponseEntity.status(201).body(checklistService.agregarPasajero(viajeId, request.pasajeroId()));
    }

    /** SRS §2.1 Nóminas dinámicas: descartar pasajero del viaje. Solo ADMIN. */
    @DeleteMapping("/{viajeId}/pasajeros/{asistenciaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChecklistItemResponse>> quitarPasajero(@PathVariable UUID viajeId,
                                                                        @PathVariable UUID asistenciaId) {
        return ResponseEntity.ok(checklistService.quitarPasajero(viajeId, asistenciaId));
    }
}
