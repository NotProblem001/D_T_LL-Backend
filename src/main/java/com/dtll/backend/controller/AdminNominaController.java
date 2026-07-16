package com.dtll.backend.controller;

import com.dtll.backend.dto.nomina.NominaRegistroResponse;
import com.dtll.backend.dto.nomina.NominaSemanaResumenResponse;
import com.dtll.backend.service.NominaConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Vistas con filtros sobre la nómina importada (planilla de horarios) para el Admin.
 * Bajo /api/v1/admin/** el SecurityConfig ya exige rol ADMIN.
 */
@RestController
@RequestMapping("/api/v1/admin/nomina")
@RequiredArgsConstructor
public class AdminNominaController {

    private final NominaConsultaService nominaConsultaService;

    @GetMapping
    public ResponseEntity<List<NominaRegistroResponse>> listar(
            @RequestParam UUID empresaId,
            @RequestParam int anio,
            @RequestParam(required = false) Integer semana,
            @RequestParam(required = false) String turno,
            @RequestParam(required = false) String comuna,
            @RequestParam(required = false) String busqueda) {
        return ResponseEntity.ok(
                nominaConsultaService.listar(empresaId, anio, semana, turno, comuna, busqueda));
    }

    /** Semanas disponibles con totales por turno, para los selectores de la vista. */
    @GetMapping("/semanas")
    public ResponseEntity<List<NominaSemanaResumenResponse>> semanas(@RequestParam UUID empresaId) {
        return ResponseEntity.ok(nominaConsultaService.semanas(empresaId));
    }
}
