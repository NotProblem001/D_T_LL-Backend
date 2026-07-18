package com.dtll.backend.repository;

import com.dtll.backend.model.entity.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ComunaRepository extends JpaRepository<Comuna, UUID> {
    Optional<Comuna> findByNombreIgnoreCase(String nombre);
}
