package com.dtll.backend.repository;

import com.dtll.backend.model.entity.Importacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImportacionRepository extends JpaRepository<Importacion, UUID> {
    List<Importacion> findByEmpresaClienteIdOrderByCreatedAtDesc(UUID empresaId);
}
