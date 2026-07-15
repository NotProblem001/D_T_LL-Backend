package com.dtll.backend.bootstrap;

import com.dtll.backend.model.entity.Usuario;
import com.dtll.backend.model.enums.ProveedorAuth;
import com.dtll.backend.model.enums.RolUsuario;
import com.dtll.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el primer usuario ADMIN a partir de ADMIN_BOOTSTRAP_EMAIL / ADMIN_BOOTSTRAP_PASSWORD
 * si aún no existe ninguna cuenta con ese email. Idempotente: no hace nada en arranques posteriores.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.bootstrap.email}")
    private String bootstrapEmail;

    @Value("${admin.bootstrap.password}")
    private String bootstrapPassword;

    @Value("${admin.bootstrap.nombre}")
    private String bootstrapNombre;

    @Override
    public void run(ApplicationArguments args) {
        if (bootstrapEmail.isBlank() || bootstrapPassword.isBlank()) {
            log.info("ADMIN_BOOTSTRAP_EMAIL/ADMIN_BOOTSTRAP_PASSWORD no configurados; se omite la creación del admin inicial.");
            return;
        }

        if (usuarioRepository.findByEmail(bootstrapEmail).isPresent()) {
            return;
        }

        usuarioRepository.save(Usuario.builder()
                .email(bootstrapEmail)
                .nombre(bootstrapNombre)
                .passwordHash(passwordEncoder.encode(bootstrapPassword))
                .rol(RolUsuario.ADMIN)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .build());

        log.info("Usuario ADMIN inicial creado para {}", bootstrapEmail);
    }
}
