package com.dtll.backend.repository;

import com.dtll.backend.model.entity.AsistenciaHistorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AsistenciaHistorialRepository extends JpaRepository<AsistenciaHistorial, UUID> {
    List<AsistenciaHistorial> findByAsistenciaIdOrderByCreatedAtDesc(UUID asistenciaId);
}
