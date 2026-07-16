package com.dtll.backend.controller;

import com.dtll.backend.dto.empresa.EmpresaClienteRequest;
import com.dtll.backend.dto.empresa.EmpresaClienteResponse;
import com.dtll.backend.service.EmpresaClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * CRUD de empresas clientes para el módulo de Clientes del Admin.
 * Bajo /api/v1/admin/** el SecurityConfig ya exige rol ADMIN.
 */
@RestController
@RequestMapping("/api/v1/admin/empresas")
@RequiredArgsConstructor
public class AdminEmpresaController {

    private final EmpresaClienteService empresaClienteService;

    @GetMapping
    public ResponseEntity<List<EmpresaClienteResponse>> listar() {
        return ResponseEntity.ok(empresaClienteService.listar());
    }

    @PostMapping
    public ResponseEntity<EmpresaClienteResponse> crear(@RequestBody EmpresaClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaClienteService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaClienteResponse> actualizar(@PathVariable UUID id,
                                                             @RequestBody EmpresaClienteRequest request) {
        return ResponseEntity.ok(empresaClienteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        empresaClienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
