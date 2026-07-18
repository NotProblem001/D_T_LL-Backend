package com.dtll.backend.controller;

import com.dtll.backend.dto.mensajeria.GuardarMensajeRequest;
import com.dtll.backend.dto.mensajeria.MensajeRutaResponse;
import com.dtll.backend.dto.mensajeria.MensajeriaViajeResponse;
import com.dtll.backend.service.MensajeriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Mensajería WhatsApp por recorrido (Etapa 5). Bajo /api/v1/mensajeria/** el
 * SecurityConfig permite ADMIN y OPERADOR.
 */
@RestController
@RequestMapping("/api/v1/mensajeria")
@RequiredArgsConstructor
public class MensajeriaController {

    private final MensajeriaService mensajeriaService;

    @GetMapping("/viajes/{viajeId}")
    public ResponseEntity<MensajeriaViajeResponse> obtener(@PathVariable UUID viajeId) {
        return ResponseEntity.ok(mensajeriaService.obtener(viajeId));
    }

    @PostMapping("/viajes/{viajeId}")
    public ResponseEntity<MensajeRutaResponse> guardar(@PathVariable UUID viajeId,
                                                       @RequestBody GuardarMensajeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mensajeriaService.guardar(viajeId, request));
    }

    @PostMapping("/mensajes/{mensajeId}/enviado")
    public ResponseEntity<MensajeRutaResponse> marcarEnviado(@PathVariable UUID mensajeId) {
        return ResponseEntity.ok(mensajeriaService.marcarEnviado(mensajeId));
    }
}
