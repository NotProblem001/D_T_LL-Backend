package com.dtll.backend.controller;

import com.dtll.backend.dto.maestros.ConductorResumenResponse;
import com.dtll.backend.model.entity.Conductor;
import com.dtll.backend.repository.ConductorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

/**
 * Consulta de conductores para selectores de maestros (vehículo/ruta habitual).
 * El CRUD completo de conductores llega con la ficha ampliada (Etapa 3).
 */
@RestController
@RequestMapping("/api/v1/maestros/conductores")
@RequiredArgsConstructor
public class ConductorConsultaController {

    private final ConductorRepository conductorRepository;

    @GetMapping
    public ResponseEntity<List<ConductorResumenResponse>> listar() {
        return ResponseEntity.ok(conductorRepository.findAll().stream()
                .sorted(Comparator.comparing(Conductor::getNombreCompleto))
                .map(ConductorResumenResponse::desde)
                .toList());
    }
}
