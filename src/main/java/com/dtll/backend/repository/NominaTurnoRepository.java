package com.dtll.backend.repository;

import com.dtll.backend.model.entity.NominaTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NominaTurnoRepository extends JpaRepository<NominaTurno, UUID> {
    List<NominaTurno> findByEmpresaClienteIdAndAnioAndSemanaOrderByTurnoAscNombreNormalizadoAsc(
            UUID empresaId, Integer anio, Integer semana);

    void deleteByEmpresaClienteIdAndAnioAndSemana(UUID empresaId, Integer anio, Integer semana);
}
