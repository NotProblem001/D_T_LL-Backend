package com.dtll.backend.repository;

import com.dtll.backend.model.entity.Conductor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConductorRepository extends JpaRepository<Conductor, UUID> {
    Optional<Conductor> findByRutConductor(String rutConductor);
}
