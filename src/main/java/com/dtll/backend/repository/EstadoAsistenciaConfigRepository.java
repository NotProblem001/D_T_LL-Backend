package com.dtll.backend.repository;

import com.dtll.backend.model.entity.EstadoAsistenciaConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstadoAsistenciaConfigRepository extends JpaRepository<EstadoAsistenciaConfig, UUID> {
    Optional<EstadoAsistenciaConfig> findByCodigo(String codigo);
    List<EstadoAsistenciaConfig> findAllByOrderByOrdenAsc();
}
