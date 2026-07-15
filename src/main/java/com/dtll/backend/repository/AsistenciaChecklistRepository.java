package com.dtll.backend.repository;

import com.dtll.backend.model.entity.AsistenciaChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AsistenciaChecklistRepository extends JpaRepository<AsistenciaChecklist, UUID> {
}
