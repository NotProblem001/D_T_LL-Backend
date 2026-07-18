package com.dtll.backend.controller;

import com.dtll.backend.dto.maestros.ActivoRequest;
import com.dtll.backend.dto.maestros.TurnoRequest;
import com.dtll.backend.dto.maestros.TurnoResponse;
import com.dtll.backend.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Maestro de turnos configurables. Lectura ADMIN/OPERADOR, escritura ADMIN. */
@RestController
@RequestMapping("/api/v1/maestros/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @GetMapping
    public ResponseEntity<List<TurnoResponse>> listar() {
        return ResponseEntity.ok(turnoService.listar());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TurnoResponse> crear(@RequestBody TurnoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TurnoResponse> actualizar(@PathVariable UUID id,
                                                    @RequestBody TurnoRequest request) {
        return ResponseEntity.ok(turnoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TurnoResponse> cambiarActivo(@PathVariable UUID id,
                                                       @RequestBody ActivoRequest request) {
        return ResponseEntity.ok(turnoService.cambiarActivo(id, Boolean.TRUE.equals(request.activo())));
    }
}
