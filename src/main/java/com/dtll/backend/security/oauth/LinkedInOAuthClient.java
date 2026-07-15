package com.dtll.backend.security.oauth;

import com.dtll.backend.dto.auth.OAuthPerfil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
public class LinkedInOAuthClient {

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public LinkedInOAuthClient(RestTemplate restTemplate,
                                @Value("${oauth.linkedin.client-id:}") String clientId,
                                @Value("${oauth.linkedin.client-secret:}") String clientSecret,
                                @Value("${oauth.linkedin.redirect-uri:}") String redirectUri) {
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    @SuppressWarnings("unchecked")
    public OAuthPerfil autenticarConCodigo(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, Object> tokenResponse;
        try {
            tokenResponse = restTemplate.postForObject(
                    "https://www.linkedin.com/oauth/v2/accessToken",
                    new HttpEntity<>(form, headers),
                    Map.class);
        } catch (Exception e) {
            log.warn("No se pudo intercambiar el código de LinkedIn: {}", e.getMessage());
            throw new IllegalArgumentException("Código de LinkedIn inválido");
        }

        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new IllegalArgumentException("Código de LinkedIn inválido");
        }
        String accessToken = String.valueOf(tokenResponse.get("access_token"));

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);

        Map<String, Object> perfil = restTemplate.exchange(
                "https://api.linkedin.com/v2/userinfo",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                Map.class).getBody();

        if (perfil == null || !perfil.containsKey("email")) {
            throw new IllegalArgumentException("No se pudo obtener el perfil de LinkedIn");
        }

        return new OAuthPerfil(
                String.valueOf(perfil.get("sub")),
                String.valueOf(perfil.get("email")),
                String.valueOf(perfil.getOrDefault("name", perfil.get("email"))));
    }
}
