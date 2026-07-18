package com.dtll.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // OPERADOR importa turnos y planifica; los maestros los edita solo ADMIN
                        // (los métodos de escritura llevan @PreAuthorize("hasRole('ADMIN')")).
                        .requestMatchers("/api/v1/importacion/**").hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers("/api/v1/maestros/**").hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers("/api/v1/planificacion/**").hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers("/api/v1/incidencias/**").hasAnyRole("ADMIN", "OPERADOR", "CONDUCTOR")
                        .requestMatchers("/api/v1/empresa/**").hasAnyRole("EMPRESA", "ADMIN")
                        .requestMatchers("/api/v1/pasajero/**").hasAnyRole("PASAJERO", "ADMIN")
                        .requestMatchers("/api/v1/conductor/**").hasAnyRole("CONDUCTOR", "ADMIN")
                        .requestMatchers("/api/v1/rutas/**").hasAnyRole("ADMIN", "CONDUCTOR")
                        .requestMatchers("/api/v1/checklist/**").hasAnyRole("ADMIN", "CONDUCTOR")
                        .requestMatchers("/api/v1/tracking/**").hasAnyRole("CONDUCTOR", "PASAJERO", "ADMIN")
                        .anyRequest().authenticated())
                // Sin token (o token expirado) → 401, para que los frontends detecten la sesión vencida
                // y redirijan al login; el 403 queda reservado para tokens válidos sin el rol requerido.
                .exceptionHandling(eh -> eh.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
