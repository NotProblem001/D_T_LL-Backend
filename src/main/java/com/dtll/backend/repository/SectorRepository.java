package com.dtll.backend.repository;

import com.dtll.backend.model.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SectorRepository extends JpaRepository<Sector, UUID> {
    Optional<Sector> findByNombreIgnoreCase(String nombre);
}
