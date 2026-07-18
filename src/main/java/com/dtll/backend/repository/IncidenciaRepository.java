package com.dtll.backend.repository;

import com.dtll.backend.model.entity.Incidencia;
import com.dtll.backend.model.enums.EstadoIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IncidenciaRepository extends JpaRepository<Incidencia, UUID> {
    List<Incidencia> findTop200ByOrderByCreatedAtDesc();
    List<Incidencia> findTop200ByEstadoOrderByCreatedAtDesc(EstadoIncidencia estado);
    List<Incidencia> findByViajeIdOrderByCreatedAtDesc(UUID viajeId);
    long countByEstado(EstadoIncidencia estado);
}
