package com.dtll.backend.repository;

import com.dtll.backend.model.entity.MensajeRuta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MensajeRutaRepository extends JpaRepository<MensajeRuta, UUID> {
    List<MensajeRuta> findByViajeIdOrderByCreatedAtDesc(UUID viajeId);
}
