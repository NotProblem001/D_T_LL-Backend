package com.dtll.backend.controller;

import com.dtll.backend.dto.importacion.NominaImportResponse;
import com.dtll.backend.service.ExcelImportService;
import com.dtll.backend.service.NominaImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/importacion")
@RequiredArgsConstructor
public class ImportacionController {

    private final ExcelImportService excelImportService;
    private final NominaImportService nominaImportService;

    /** Importa la BDD interna de pasajeros (hoja Nombre|Telefono|Dirección|Comuna). */
    @PostMapping("/bdd")
    public ResponseEntity<NominaImportResponse> importarBdd(@RequestParam("file") MultipartFile file,
                                                            @RequestParam UUID empresaId) {
        return ResponseEntity.ok(nominaImportService.importarBddPasajeros(empresaId, file));
    }

    /** Importa la nómina semanal de turnos enviada por la empresa cliente (ej: SEM 29.xlsx). */
    @PostMapping("/nomina")
    public ResponseEntity<NominaImportResponse> importarNomina(@RequestParam("file") MultipartFile file,
                                                               @RequestParam UUID empresaId,
                                                               @RequestParam(required = false) Integer anio,
                                                               @RequestParam(required = false) Integer semana) {
        return ResponseEntity.ok(nominaImportService.importarNominaSemanal(empresaId, anio, semana, file));
    }

    /** Importa la nómina semanal desde texto pegado (formato correo). */
    @PostMapping("/nomina/texto")
    public ResponseEntity<NominaImportResponse> importarNominaTexto(@RequestBody Map<String, String> body) {
        UUID empresaId = UUID.fromString(body.get("empresaId"));
        Integer anio = body.get("anio") != null ? Integer.valueOf(body.get("anio")) : null;
        Integer semana = body.get("semana") != null ? Integer.valueOf(body.get("semana")) : null;
        return ResponseEntity.ok(nominaImportService.importarNominaTexto(empresaId, anio, semana, body.get("texto")));
    }

    /** Descarga la Planilla de Horarios generada para una semana importada. */
    @GetMapping("/nomina/planilla")
    public ResponseEntity<byte[]> descargarPlanilla(@RequestParam UUID empresaId,
                                                    @RequestParam int anio,
                                                    @RequestParam int semana) {
        byte[] xlsx = nominaImportService.generarPlanillaHorarios(empresaId, anio, semana);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"Planilla horarios semana " + semana + " " + anio + ".xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

    @PostMapping("/excel")
    public ResponseEntity<Map<String, String>> importarExcel(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();
        try {
            String mensaje = excelImportService.procesarExcel(file);
            response.put("message", mensaje);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("error", "Error interno del servidor al procesar el archivo.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
