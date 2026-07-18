package com.dtll.backend.controller;

import com.dtll.backend.dto.planificacion.*;
import com.dtll.backend.service.PlanificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Planificación de recorridos (Etapa 3). Bajo /api/v1/planificacion/** el
 * SecurityConfig permite ADMIN y OPERADOR.
 */
@RestController
@RequestMapping("/api/v1/planificacion")
@RequiredArgsConstructor
public class PlanificacionController {

    private final PlanificacionService planificacionService;

    @PostMapping("/generar")
    public ResponseEntity<PropuestaResponse> generar(@RequestBody GenerarPropuestaRequest request) {
        return ResponseEntity.ok(planificacionService.generarPropuesta(request));
    }

    @GetMapping("/viajes")
    public ResponseEntity<List<ViajeResumenResponse>> viajes(
            @RequestParam UUID empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(planificacionService.listarPorFecha(empresaId, fecha));
    }

    @PutMapping("/viajes/{id}/asignacion")
    public ResponseEntity<ViajeResumenResponse> asignar(@PathVariable UUID id,
                                                        @RequestBody AsignacionRequest request) {
        return ResponseEntity.ok(planificacionService.asignar(id, request));
    }

    @PutMapping("/viajes/{id}/estado")
    public ResponseEntity<ViajeResumenResponse> cambiarEstado(@PathVariable UUID id,
                                                              @RequestBody CambiarEstadoRequest request) {
        return ResponseEntity.ok(planificacionService.cambiarEstado(id, request.estado()));
    }

    @DeleteMapping("/viajes/{id}")
    public ResponseEntity<Void> eliminarBorrador(@PathVariable UUID id) {
        planificacionService.eliminarBorrador(id);
        return ResponseEntity.noContent().build();
    }
}
