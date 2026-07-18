package com.dtll.backend.controller;

import com.dtll.backend.dto.maestros.ActivoRequest;
import com.dtll.backend.dto.maestros.ComunaRequest;
import com.dtll.backend.dto.maestros.ComunaResponse;
import com.dtll.backend.dto.maestros.SectorRequest;
import com.dtll.backend.dto.maestros.SectorResponse;
import com.dtll.backend.service.GeografiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Maestros de geografía: comunas y sectores. Lectura ADMIN/OPERADOR, escritura ADMIN. */
@RestController
@RequestMapping("/api/v1/maestros")
@RequiredArgsConstructor
public class GeografiaController {

    private final GeografiaService geografiaService;

    // ------------------------------------------------------------------ comunas

    @GetMapping("/comunas")
    public ResponseEntity<List<ComunaResponse>> listarComunas() {
        return ResponseEntity.ok(geografiaService.listarComunas());
    }

    @PostMapping("/comunas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComunaResponse> crearComuna(@RequestBody ComunaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(geografiaService.crearComuna(request));
    }

    @PutMapping("/comunas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComunaResponse> actualizarComuna(@PathVariable UUID id,
                                                           @RequestBody ComunaRequest request) {
        return ResponseEntity.ok(geografiaService.actualizarComuna(id, request));
    }

    @PatchMapping("/comunas/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComunaResponse> cambiarActivoComuna(@PathVariable UUID id,
                                                              @RequestBody ActivoRequest request) {
        return ResponseEntity.ok(
                geografiaService.cambiarActivoComuna(id, Boolean.TRUE.equals(request.activo())));
    }

    // ------------------------------------------------------------------ sectores

    @GetMapping("/sectores")
    public ResponseEntity<List<SectorResponse>> listarSectores() {
        return ResponseEntity.ok(geografiaService.listarSectores());
    }

    @PostMapping("/sectores")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectorResponse> crearSector(@RequestBody SectorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(geografiaService.crearSector(request));
    }

    @PutMapping("/sectores/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectorResponse> actualizarSector(@PathVariable UUID id,
                                                           @RequestBody SectorRequest request) {
        return ResponseEntity.ok(geografiaService.actualizarSector(id, request));
    }

    @PatchMapping("/sectores/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectorResponse> cambiarActivoSector(@PathVariable UUID id,
                                                              @RequestBody ActivoRequest request) {
        return ResponseEntity.ok(
                geografiaService.cambiarActivoSector(id, Boolean.TRUE.equals(request.activo())));
    }
}
