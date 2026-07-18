package com.dtll.backend.repository;

import com.dtll.backend.model.entity.ViajeCambio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ViajeCambioRepository extends JpaRepository<ViajeCambio, UUID> {
    List<ViajeCambio> findByViajeIdOrderByCreatedAtAsc(UUID viajeId);
}
