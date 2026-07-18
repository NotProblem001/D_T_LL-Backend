package com.dtll.backend.repository;

import com.dtll.backend.model.entity.ImportacionRegistro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImportacionRegistroRepository extends JpaRepository<ImportacionRegistro, UUID> {
    List<ImportacionRegistro> findByImportacionIdOrderByHojaOrigenAscFilaOrigenAsc(UUID importacionId);
}
