package com.dtll.backend.controller;

import com.dtll.backend.dto.importacion.*;
import com.dtll.backend.model.enums.TipoImportacion;
import com.dtll.backend.service.ImportacionRevisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Importación con revisión: preview → resolver sugerencias → confirmar.
 * Bajo /api/v1/importacion/** el SecurityConfig permite ADMIN y OPERADOR.
 */
@RestController
@RequestMapping("/api/v1/importacion/revision")
@RequiredArgsConstructor
public class ImportacionRevisionController {

    private final ImportacionRevisionService revisionService;

    @PostMapping("/preview")
    public ResponseEntity<ImportacionDetalleResponse> preview(
            @RequestParam("file") MultipartFile file,
            @RequestParam UUID empresaId,
            @RequestParam TipoImportacion tipo,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer semana,
            @AuthenticationPrincipal String usuarioId) {
        return ResponseEntity.ok(revisionService.previewArchivo(
                empresaId, tipo, file, anio, semana, aUuid(usuarioId)));
    }

    @PostMapping("/preview/texto")
    public ResponseEntity<ImportacionDetalleResponse> previewTexto(
            @RequestBody PreviewTextoRequest request,
            @AuthenticationPrincipal String usuarioId) {
        return ResponseEntity.ok(revisionService.previewTexto(request, aUuid(usuarioId)));
    }

    @GetMapping
    public ResponseEntity<List<ImportacionResponse>> listar(@RequestParam UUID empresaId) {
        return ResponseEntity.ok(revisionService.listar(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImportacionDetalleResponse> detalle(@PathVariable UUID id) {
        return ResponseEntity.ok(revisionService.detalle(id));
    }

    @PutMapping("/{id}/registros/{registroId}")
    public ResponseEntity<RegistroImportacionResponse> resolver(
            @PathVariable UUID id,
            @PathVariable UUID registroId,
            @RequestBody ResolverRegistroRequest request) {
        return ResponseEntity.ok(revisionService.resolverRegistro(id, registroId, request));
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<ImportacionResponse> confirmar(@PathVariable UUID id) {
        return ResponseEntity.ok(revisionService.confirmar(id));
    }

    @PostMapping("/{id}/descartar")
    public ResponseEntity<ImportacionResponse> descartar(@PathVariable UUID id) {
        return ResponseEntity.ok(revisionService.descartar(id));
    }

    private UUID aUuid(String usuarioId) {
        try {
            return usuarioId != null ? UUID.fromString(usuarioId) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
