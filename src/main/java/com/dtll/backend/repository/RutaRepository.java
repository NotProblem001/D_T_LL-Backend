package com.dtll.backend.repository;

import com.dtll.backend.model.entity.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RutaRepository extends JpaRepository<Ruta, UUID> {
    List<Ruta> findByEmpresaClienteId(UUID empresaId);
    Optional<Ruta> findByEmpresaClienteIdAndNombreIgnoreCase(UUID empresaId, String nombre);
}
