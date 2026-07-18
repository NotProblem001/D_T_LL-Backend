package com.dtll.backend.repository;

import com.dtll.backend.model.entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VehiculoRepository extends JpaRepository<Vehiculo, UUID> {
    Optional<Vehiculo> findByPatente(String patente);
}
