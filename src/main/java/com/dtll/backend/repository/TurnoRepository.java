package com.dtll.backend.repository;

import com.dtll.backend.model.entity.Turno;
import com.dtll.backend.model.enums.TipoTrayecto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TurnoRepository extends JpaRepository<Turno, UUID> {
    Optional<Turno> findByNombreIgnoreCaseAndTipoServicio(String nombre, TipoTrayecto tipoServicio);
}
