package com.dtll.backend.controller;

import com.dtll.backend.dto.maestros.ActivoRequest;
import com.dtll.backend.dto.maestros.VehiculoRequest;
import com.dtll.backend.dto.maestros.VehiculoResponse;
import com.dtll.backend.service.VehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Maestro de vehículos. Bajo /api/v1/maestros/** el SecurityConfig permite
 * consultar a ADMIN y OPERADOR; crear/editar/desactivar queda solo para ADMIN.
 */
@RestController
@RequestMapping("/api/v1/maestros/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;

    @GetMapping
    public ResponseEntity<List<VehiculoResponse>> listar() {
        return ResponseEntity.ok(vehiculoService.listar());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehiculoResponse> crear(@RequestBody VehiculoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehiculoResponse> actualizar(@PathVariable UUID id,
                                                       @RequestBody VehiculoRequest request) {
        return ResponseEntity.ok(vehiculoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehiculoResponse> cambiarActivo(@PathVariable UUID id,
                                                          @RequestBody ActivoRequest request) {
        return ResponseEntity.ok(vehiculoService.cambiarActivo(id, Boolean.TRUE.equals(request.activo())));
    }
}
