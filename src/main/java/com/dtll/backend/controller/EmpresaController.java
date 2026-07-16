package com.dtll.backend.controller;

import com.dtll.backend.dto.empresa.EmpresaListItemResponse;
import com.dtll.backend.dto.empresa.PasajeroEmpresaResponse;
import com.dtll.backend.dto.empresa.ReporteFacturacionResponse;
import com.dtll.backend.dto.empresa.ResumenEmpresaResponse;
import com.dtll.backend.security.AuthenticatedUser;
import com.dtll.backend.service.EmpresaPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Portal B2B: cada empresa cliente solo ve sus propios datos (empresaId resuelto desde el JWT).
 * Los usuarios ADMIN pueden consultar cualquier empresa pasando ?empresaId=.
 */
@RestController
@RequestMapping("/api/v1/empresa")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaPortalService empresaPortalService;

    /** Listado de empresas para el selector del ADMIN. */
    @GetMapping("/empresas")
    public ResponseEntity<List<EmpresaListItemResponse>> empresas() {
        if (!AuthenticatedUser.esAdmin()) {
            throw new IllegalArgumentException("Solo un ADMIN puede listar las empresas");
        }
        return ResponseEntity.ok(empresaPortalService.empresas());
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenEmpresaResponse> resumen(@RequestParam(required = false) UUID empresaId) {
        return ResponseEntity.ok(empresaPortalService.resumen(resolverEmpresaId(empresaId)));
    }

    @GetMapping("/reportes-facturacion")
    public ResponseEntity<List<ReporteFacturacionResponse>> reportesFacturacion(
            @RequestParam(required = false) UUID empresaId) {
        return ResponseEntity.ok(empresaPortalService.reportesFacturacion(resolverEmpresaId(empresaId)));
    }

    @GetMapping("/pasajeros")
    public ResponseEntity<List<PasajeroEmpresaResponse>> pasajeros(
            @RequestParam(required = false) UUID empresaId) {
        return ResponseEntity.ok(empresaPortalService.pasajeros(resolverEmpresaId(empresaId)));
    }

    /**
     * EMPRESA: siempre su propio tenant (claim del JWT).
     * ADMIN: la empresa indicada por parámetro.
     */
    private UUID resolverEmpresaId(UUID empresaIdParam) {
        if (AuthenticatedUser.esAdmin() && empresaIdParam != null) {
            return empresaIdParam;
        }
        return AuthenticatedUser.empresaId();
    }
}
