package com.dtll.backend.controller;

import com.dtll.backend.dto.maestros.ActivoRequest;
import com.dtll.backend.dto.maestros.ConfiguracionResponse;
import com.dtll.backend.dto.maestros.ConfiguracionUpdateRequest;
import com.dtll.backend.dto.maestros.EstadoAsistenciaRequest;
import com.dtll.backend.dto.maestros.EstadoAsistenciaResponse;
import com.dtll.backend.service.ConfiguracionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Parámetros de operación y estados de asistencia configurables. Lectura ADMIN/OPERADOR, escritura ADMIN. */
@RestController
@RequestMapping("/api/v1/maestros")
@RequiredArgsConstructor
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    // ------------------------------------------------------------- configuraciones

    @GetMapping("/configuraciones")
    public ResponseEntity<List<ConfiguracionResponse>> listarConfiguraciones() {
        return ResponseEntity.ok(configuracionService.listarConfiguraciones());
    }

    @PutMapping("/configuraciones/{clave}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracionResponse> actualizarConfiguracion(
            @PathVariable String clave,
            @RequestBody ConfiguracionUpdateRequest request) {
        return ResponseEntity.ok(configuracionService.actualizarConfiguracion(clave, request.valor()));
    }

    // --------------------------------------------------------- estados de asistencia

    @GetMapping("/estados-asistencia")
    public ResponseEntity<List<EstadoAsistenciaResponse>> listarEstadosAsistencia() {
        return ResponseEntity.ok(configuracionService.listarEstadosAsistencia());
    }

    @PostMapping("/estados-asistencia")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EstadoAsistenciaResponse> crearEstadoAsistencia(
            @RequestBody EstadoAsistenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(configuracionService.crearEstadoAsistencia(request));
    }

    @PutMapping("/estados-asistencia/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EstadoAsistenciaResponse> actualizarEstadoAsistencia(
            @PathVariable UUID id,
            @RequestBody EstadoAsistenciaRequest request) {
        return ResponseEntity.ok(configuracionService.actualizarEstadoAsistencia(id, request));
    }

    @PatchMapping("/estados-asistencia/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EstadoAsistenciaResponse> cambiarActivoEstadoAsistencia(
            @PathVariable UUID id,
            @RequestBody ActivoRequest request) {
        return ResponseEntity.ok(configuracionService.cambiarActivoEstadoAsistencia(
                id, Boolean.TRUE.equals(request.activo())));
    }
}
