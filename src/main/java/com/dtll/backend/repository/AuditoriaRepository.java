package com.dtll.backend.repository;

import com.dtll.backend.model.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditoriaRepository extends JpaRepository<Auditoria, UUID> {
    List<Auditoria> findTop200ByOrderByCreatedAtDesc();
    List<Auditoria> findTop200ByModuloOrderByCreatedAtDesc(String modulo);
    List<Auditoria> findByRegistroIdOrderByCreatedAtDesc(UUID registroId);
}
