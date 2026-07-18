package com.dtll.backend.service;

import com.dtll.backend.dto.auth.*;
import com.dtll.backend.model.entity.Conductor;
import com.dtll.backend.model.entity.Usuario;
import com.dtll.backend.model.enums.ProveedorAuth;
import com.dtll.backend.model.enums.RolUsuario;
import com.dtll.backend.repository.ConductorRepository;
import com.dtll.backend.repository.UsuarioRepository;
import com.dtll.backend.security.JwtService;
import com.dtll.backend.security.oauth.GoogleTokenVerifier;
import com.dtll.backend.security.oauth.LinkedInOAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ConductorRepository conductorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final LinkedInOAuthClient linkedInOAuthClient;

    public String login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.name())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (usuario.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("Usuario inactivo");
        }

        return jwtService.generarToken(usuario);
    }

    @Transactional
    public String loginConGoogle(String idToken) {
        OAuthPerfil perfil = googleTokenVerifier.verificar(idToken);
        Usuario usuario = obtenerOCrearUsuarioOAuth(perfil, ProveedorAuth.GOOGLE);
        return jwtService.generarToken(usuario);
    }

    @Transactional
    public String loginConLinkedIn(String code) {
        OAuthPerfil perfil = linkedInOAuthClient.autenticarConCodigo(code);
        Usuario usuario = obtenerOCrearUsuarioOAuth(perfil, ProveedorAuth.LINKEDIN);
        return jwtService.generarToken(usuario);
    }

    private Usuario obtenerOCrearUsuarioOAuth(OAuthPerfil perfil, ProveedorAuth proveedor) {
        return usuarioRepository.findByProveedorAuthAndOauthId(proveedor, perfil.oauthId())
                .or(() -> usuarioRepository.findByEmail(perfil.email()))
                .orElseGet(() -> usuarioRepository.save(Usuario.builder()
                        .email(perfil.email())
                        .nombre(perfil.nombre())
                        .rol(RolUsuario.PASAJERO)
                        .proveedorAuth(proveedor)
                        .oauthId(perfil.oauthId())
                        .build()));
    }

    @Transactional
    public Usuario registrar(RegisterRequest request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese email");
        }

        return usuarioRepository.save(Usuario.builder()
                .email(request.email())
                .nombre(request.nombre())
                .passwordHash(passwordEncoder.encode(request.password()))
                .rol(RolUsuario.PASAJERO)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .build());
    }

    @Transactional
    public Usuario actualizar(java.util.UUID usuarioId, UpdateUserRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (request.nombre() != null && !request.nombre().isBlank()) {
            usuario.setNombre(request.nombre());
        }
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return usuarioRepository.save(usuario);
    }

    public String loginConductor(ConductorLoginRequest request) {
        // El RUT se guarda normalizado (sin puntos, con guión): se busca igual,
        // sin importar cómo lo escriba el conductor en su teléfono.
        String rut = com.dtll.backend.util.RutUtil.normalizar(request.rutConductor());
        Conductor conductor = conductorRepository.findByRutConductor(rut)
                .orElseThrow(() -> new IllegalArgumentException("RUT o PIN incorrectos"));

        String pin = request.pin() == null ? "" : request.pin().trim();
        if (conductor.getPinAccesoHash() == null
                || !passwordEncoder.matches(pin, conductor.getPinAccesoHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        if (!Boolean.TRUE.equals(conductor.getActivo())) {
            throw new IllegalArgumentException("Conductor inactivo");
        }

        return jwtService.generarTokenConductor(conductor.getId(), conductor.getNombreCompleto());
    }
}
