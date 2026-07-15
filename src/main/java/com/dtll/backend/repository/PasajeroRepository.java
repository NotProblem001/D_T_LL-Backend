package com.dtll.backend.repository;

import com.dtll.backend.model.entity.Pasajero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasajeroRepository extends JpaRepository<Pasajero, UUID> {
    Optional<Pasajero> findByIdentificadorInterno(String identificadorInterno);
}
