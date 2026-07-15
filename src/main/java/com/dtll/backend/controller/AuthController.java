package com.dtll.backend.controller;

import com.dtll.backend.dto.auth.*;
import com.dtll.backend.model.entity.Usuario;
import com.dtll.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/token", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> token(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping(value = "/google", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> google(@RequestBody OAuthLoginRequest request) {
        return ResponseEntity.ok(authService.loginConGoogle(request.token()));
    }

    @PostMapping(value = "/linkedin", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> linkedin(@RequestBody LinkedInLoginRequest request) {
        return ResponseEntity.ok(authService.loginConLinkedIn(request.code()));
    }

    @PostMapping(value = "/conductor/login", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> conductorLogin(@RequestBody ConductorLoginRequest request) {
        return ResponseEntity.ok(authService.loginConductor(request));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
        authService.registrar(request);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> update(@AuthenticationPrincipal String usuarioId,
                                        @RequestBody UpdateUserRequest request) {
        authService.actualizar(UUID.fromString(usuarioId), request);
        return ResponseEntity.noContent().build();
    }
}
