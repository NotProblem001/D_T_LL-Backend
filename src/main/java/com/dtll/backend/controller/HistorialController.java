package com.dtll.backend.controller;

import com.dtll.backend.dto.historial.AuditoriaResponse;
import com.dtll.backend.dto.historial.HistorialDetalleResponse;
import com.dtll.backend.dto.historial.HistorialViajeResponse;
import com.dtll.backend.model.enums.EstadoViaje;
import com.dtll.backend.service.HistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Historial de recorridos y auditoría (Etapa 6). Bajo /api/v1/historial/** el
 * SecurityConfig permite ADMIN y OPERADOR; la auditoría es solo ADMIN.
 */
@RestController
@RequestMapping("/api/v1/historial")
@RequiredArgsConstructor
public class HistorialController {

    private final HistorialService historialService;

    @GetMapping("/viajes")
    public ResponseEntity<List<HistorialViajeResponse>> buscar(
            @RequestParam UUID empresaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) UUID conductorId,
            @RequestParam(required = false) UUID vehiculoId,
            @RequestParam(required = false) UUID rutaId,
            @RequestParam(required = false) String jornada,
            @RequestParam(required = false) String tipoTrayecto,
            @RequestParam(required = false) EstadoViaje estado,
            @RequestParam(required = false) UUID pasajeroId) {
        return ResponseEntity.ok(historialService.buscar(empresaId, desde, hasta, conductorId,
                vehiculoId, rutaId, jornada, tipoTrayecto, estado, pasajeroId));
    }

    @GetMapping("/viajes/{id}")
    public ResponseEntity<HistorialDetalleResponse> detalle(@PathVariable UUID id) {
        return ResponseEntity.ok(historialService.detalle(id));
    }

    @GetMapping("/auditoria")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditoriaResponse>> auditoria(
            @RequestParam(required = false) String modulo) {
        return ResponseEntity.ok(historialService.auditoria(modulo));
    }
}
