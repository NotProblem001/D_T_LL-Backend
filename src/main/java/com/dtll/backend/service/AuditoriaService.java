package com.dtll.backend.service;

import com.dtll.backend.model.entity.Auditoria;
import com.dtll.backend.repository.AuditoriaRepository;
import com.dtll.backend.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Auditoría transversal (sección 21). Best-effort: un fallo al auditar nunca
 * debe romper la operación principal.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public void registrar(String accion, String modulo, UUID registroId, String descripcion) {
        registrar(accion, modulo, registroId, descripcion, null, null);
    }

    public void registrar(String accion, String modulo, UUID registroId, String descripcion,
                          String datosAnterior, String datosNuevo) {
        try {
            auditoriaRepository.save(Auditoria.builder()
                    .usuarioId(AuthenticatedUser.subjectId())
                    .usuarioRol(AuthenticatedUser.rol())
                    .accion(accion)
                    .modulo(modulo)
                    .registroId(registroId)
                    .descripcion(descripcion)
                    .datosAnterior(datosAnterior)
                    .datosNuevo(datosNuevo)
                    .build());
        } catch (Exception e) {
            log.warn("No se pudo registrar la auditoría {} {}: {}", accion, modulo, e.getMessage());
        }
    }
}
