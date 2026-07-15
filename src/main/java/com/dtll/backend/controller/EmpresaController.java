package com.dtll.backend.controller;

import com.dtll.backend.dto.empresa.PasajeroEmpresaResponse;
import com.dtll.backend.dto.empresa.ReporteFacturacionResponse;
import com.dtll.backend.dto.empresa.ResumenEmpresaResponse;
import com.dtll.backend.security.AuthenticatedUser;
import com.dtll.backend.service.EmpresaPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Portal B2B: cada empresa cliente solo ve sus propios datos (empresaId resuelto desde el JWT). */
@RestController
@RequestMapping("/api/v1/empresa")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaPortalService empresaPortalService;

    @GetMapping("/resumen")
    public ResponseEntity<ResumenEmpresaResponse> resumen() {
        return ResponseEntity.ok(empresaPortalService.resumen(AuthenticatedUser.empresaId()));
    }

    @GetMapping("/reportes-facturacion")
    public ResponseEntity<List<ReporteFacturacionResponse>> reportesFacturacion() {
        return ResponseEntity.ok(empresaPortalService.reportesFacturacion(AuthenticatedUser.empresaId()));
    }

    @GetMapping("/pasajeros")
    public ResponseEntity<List<PasajeroEmpresaResponse>> pasajeros() {
        return ResponseEntity.ok(empresaPortalService.pasajeros(AuthenticatedUser.empresaId()));
    }
}
