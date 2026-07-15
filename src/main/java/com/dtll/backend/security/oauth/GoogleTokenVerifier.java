package com.dtll.backend.security.oauth;

import com.dtll.backend.dto.auth.OAuthPerfil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
public class GoogleTokenVerifier {

    private final RestTemplate restTemplate;
    private final String googleClientId;

    public GoogleTokenVerifier(RestTemplate restTemplate,
                                @Value("${oauth.google.client-id:}") String googleClientId) {
        this.restTemplate = restTemplate;
        this.googleClientId = googleClientId;
    }

    @SuppressWarnings("unchecked")
    public OAuthPerfil verificar(String idToken) {
        Map<String, Object> body;
        try {
            body = restTemplate.getForObject(
                    "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken, Map.class);
        } catch (Exception e) {
            log.warn("Token de Google inválido: {}", e.getMessage());
            throw new IllegalArgumentException("Token de Google inválido");
        }

        if (body == null || !body.containsKey("email")) {
            throw new IllegalArgumentException("Token de Google inválido");
        }

        String aud = String.valueOf(body.get("aud"));
        if (!googleClientId.isBlank() && !googleClientId.equals(aud)) {
            throw new IllegalArgumentException("El token de Google no corresponde a esta aplicación");
        }

        return new OAuthPerfil(
                String.valueOf(body.get("sub")),
                String.valueOf(body.get("email")),
                String.valueOf(body.getOrDefault("name", body.get("email"))));
    }
}
