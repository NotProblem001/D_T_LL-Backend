package com.dtll.backend.controller;

import com.dtll.backend.optimization.RouteOptimizationService;
import com.dtll.backend.optimization.dto.OptimizarRutaRequest;
import com.dtll.backend.optimization.dto.ParadaOptimizadaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rutas")
@RequiredArgsConstructor
public class RutaOptimizacionController {

    private final RouteOptimizationService routeOptimizationService;

    @PostMapping("/{viajeId}/optimizar")
    public ResponseEntity<List<ParadaOptimizadaResponse>> optimizar(@PathVariable UUID viajeId,
                                                                      @RequestBody OptimizarRutaRequest request) {
        return ResponseEntity.ok(routeOptimizationService.optimizar(viajeId, request));
    }

    @GetMapping("/{viajeId}/paradas")
    public ResponseEntity<List<ParadaOptimizadaResponse>> paradas(@PathVariable UUID viajeId) {
        return ResponseEntity.ok(routeOptimizationService.obtenerParadas(viajeId));
    }
}
