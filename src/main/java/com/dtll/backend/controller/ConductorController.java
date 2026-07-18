package com.dtll.backend.controller;

import com.dtll.backend.dto.maestros.ActivoRequest;
import com.dtll.backend.dto.maestros.ConductorRequest;
import com.dtll.backend.dto.maestros.ConductorResponse;
import com.dtll.backend.service.ConductorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * CRUD de conductores. Lectura ADMIN/OPERADOR (los selectores de vehículos y
 * rutas la usan), escritura solo ADMIN.
 */
@RestController
@RequestMapping("/api/v1/maestros/conductores")
@RequiredArgsConstructor
public class ConductorController {

    private final ConductorService conductorService;

    @GetMapping
    public ResponseEntity<List<ConductorResponse>> listar() {
        return ResponseEntity.ok(conductorService.listar());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConductorResponse> crear(@RequestBody ConductorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(conductorService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConductorResponse> actualizar(@PathVariable UUID id,
                                                        @RequestBody ConductorRequest request) {
        return ResponseEntity.ok(conductorService.actualizar(id, request));
    }

    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConductorResponse> cambiarActivo(@PathVariable UUID id,
                                                           @RequestBody ActivoRequest request) {
        return ResponseEntity.ok(conductorService.cambiarActivo(id, Boolean.TRUE.equals(request.activo())));
    }
}
