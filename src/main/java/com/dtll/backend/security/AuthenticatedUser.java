package com.dtll.backend.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class AuthenticatedUser {

    private AuthenticatedUser() {
    }

    public static UUID empresaId() {
        Claims claims = claimsActuales();
        String empresaId = claims.get("empresaId", String.class);
        if (empresaId == null) {
            throw new IllegalStateException("El usuario autenticado no está asociado a una empresa");
        }
        return UUID.fromString(empresaId);
    }

    /** Subject del token: usuarioId (login normal) o conductorId (login por código/PIN). */
    public static UUID subjectId() {
        return UUID.fromString(claimsActuales().getSubject());
    }

    public static String rol() {
        return claimsActuales().get("rol", String.class);
    }

    public static boolean esAdmin() {
        return "ADMIN".equals(rol());
    }

    /** pasajeroId del claim, o null si el usuario no es pasajero. */
    public static UUID pasajeroIdONull() {
        String pasajeroId = claimsActuales().get("pasajeroId", String.class);
        return pasajeroId == null ? null : UUID.fromString(pasajeroId);
    }

    public static Claims claimsActuales() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getDetails() instanceof Claims claims)) {
            throw new IllegalStateException("No hay una sesión autenticada válida");
        }
        return claims;
    }
}
