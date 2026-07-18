package com.dtll.backend.repository;

import com.dtll.backend.model.entity.AsistenciaChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AsistenciaChecklistRepository extends JpaRepository<AsistenciaChecklist, UUID> {
    List<AsistenciaChecklist> findByViajeId(UUID viajeId);
    boolean existsByViajeIdAndPasajeroId(UUID viajeId, UUID pasajeroId);
    java.util.Optional<AsistenciaChecklist> findByIdAndViajeId(UUID id, UUID viajeId);
    long countByViajeId(UUID viajeId);
}
