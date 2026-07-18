package com.dtll.backend.controller;

import com.dtll.backend.dto.conductor.ViajeConductorResponse;
import com.dtll.backend.dto.maestros.EstadoAsistenciaResponse;
import com.dtll.backend.service.ConductorViajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Vista móvil del conductor. Bajo /api/v1/conductor/** el SecurityConfig
 * permite CONDUCTOR y ADMIN; la propiedad del viaje la valida ViajeAccessGuard.
 */
@RestController
@RequestMapping("/api/v1/conductor")
@RequiredArgsConstructor
public class ConductorViajeController {

    private final ConductorViajeService conductorViajeService;

    @GetMapping("/viajes")
    public ResponseEntity<List<ViajeConductorResponse>> misViajes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(conductorViajeService.misViajes(fecha));
    }

    @GetMapping("/viajes/{id}")
    public ResponseEntity<ViajeConductorResponse> detalle(@PathVariable UUID id) {
        return ResponseEntity.ok(conductorViajeService.detalle(id));
    }

    @PostMapping("/viajes/{id}/confirmar")
    public ResponseEntity<ViajeConductorResponse> confirmar(@PathVariable UUID id) {
        return ResponseEntity.ok(conductorViajeService.confirmar(id));
    }

    @PostMapping("/viajes/{id}/iniciar")
    public ResponseEntity<ViajeConductorResponse> iniciar(@PathVariable UUID id) {
        return ResponseEntity.ok(conductorViajeService.iniciar(id));
    }

    @PostMapping("/viajes/{id}/finalizar")
    public ResponseEntity<ViajeConductorResponse> finalizar(@PathVariable UUID id) {
        return ResponseEntity.ok(conductorViajeService.finalizar(id));
    }

    @GetMapping("/estados-asistencia")
    public ResponseEntity<List<EstadoAsistenciaResponse>> estadosAsistencia() {
        return ResponseEntity.ok(conductorViajeService.estadosAsistencia());
    }
}
