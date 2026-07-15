package com.dtll.backend.security;

import com.dtll.backend.model.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expirationMs}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("rol", usuario.getRol().name());
        claims.put("nombre", usuario.getNombre());
        claims.put("email", usuario.getEmail());
        claims.put("name", usuario.getNombre());
        if (usuario.getEmpresaCliente() != null) {
            claims.put("empresaId", usuario.getEmpresaCliente().getId().toString());
        }
        if (usuario.getPasajero() != null) {
            claims.put("pasajeroId", usuario.getPasajero().getId().toString());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(usuario.getId().toString())
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generarTokenConductor(UUID conductorId, String nombreCompleto) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(Map.of("rol", "CONDUCTOR", "nombre", nombreCompleto))
                .setSubject(conductorId.toString())
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean esTokenValido(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
