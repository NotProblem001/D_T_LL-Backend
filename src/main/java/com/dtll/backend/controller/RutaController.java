package com.dtll.backend.controller;

import com.dtll.backend.dto.maestros.ActivoRequest;
import com.dtll.backend.dto.maestros.RutaRequest;
import com.dtll.backend.dto.maestros.RutaResponse;
import com.dtll.backend.service.RutaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Maestro de rutas por empresa. Lectura ADMIN/OPERADOR, escritura ADMIN. */
@RestController
@RequestMapping("/api/v1/maestros/rutas")
@RequiredArgsConstructor
public class RutaController {

    private final RutaService rutaService;

    @GetMapping
    public ResponseEntity<List<RutaResponse>> listar(@RequestParam(required = false) UUID empresaId) {
        return ResponseEntity.ok(rutaService.listar(empresaId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RutaResponse> crear(@RequestBody RutaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rutaService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RutaResponse> actualizar(@PathVariable UUID id,
                                                   @RequestBody RutaRequest request) {
        return ResponseEntity.ok(rutaService.actualizar(id, request));
    }

    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RutaResponse> cambiarActivo(@PathVariable UUID id,
                                                      @RequestBody ActivoRequest request) {
        return ResponseEntity.ok(rutaService.cambiarActivo(id, Boolean.TRUE.equals(request.activo())));
    }
}
