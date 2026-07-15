package com.dtll.backend.repository;

import com.dtll.backend.model.entity.Viaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ViajeRepository extends JpaRepository<Viaje, UUID> {
    boolean existsByCodigoRutaLogin(String codigoRutaLogin);
}
