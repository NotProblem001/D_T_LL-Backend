package com.dtll.backend.repository;

import com.dtll.backend.model.entity.ViajeParada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ViajeParadaRepository extends JpaRepository<ViajeParada, UUID> {
    List<ViajeParada> findByViajeIdOrderByOrdenParadaAsc(UUID viajeId);
    void deleteByViajeId(UUID viajeId);
}
