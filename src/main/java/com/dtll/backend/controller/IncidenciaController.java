package com.dtll.backend.controller;

import com.dtll.backend.dto.incidencia.IncidenciaRequest;
import com.dtll.backend.dto.incidencia.IncidenciaResponse;
import com.dtll.backend.dto.incidencia.IncidenciaUpdateRequest;
import com.dtll.backend.model.enums.EstadoIncidencia;
import com.dtll.backend.service.IncidenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Incidencias. Crear: ADMIN/OPERADOR/CONDUCTOR (el conductor solo sobre sus
 * viajes). Listar y gestionar: ADMIN/OPERADOR.
 */
@RestController
@RequestMapping("/api/v1/incidencias")
@RequiredArgsConstructor
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    @PostMapping
    public ResponseEntity<IncidenciaResponse> crear(@RequestBody IncidenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidenciaService.crear(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<List<IncidenciaResponse>> listar(
            @RequestParam(required = false) EstadoIncidencia estado,
            @RequestParam(required = false) UUID viajeId) {
        return ResponseEntity.ok(incidenciaService.listar(estado, viajeId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<IncidenciaResponse> actualizar(@PathVariable UUID id,
                                                         @RequestBody IncidenciaUpdateRequest request) {
        return ResponseEntity.ok(incidenciaService.actualizar(id, request));
    }
}
