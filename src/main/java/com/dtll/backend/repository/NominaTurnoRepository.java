package com.dtll.backend.repository;

import com.dtll.backend.model.entity.NominaTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NominaTurnoRepository extends JpaRepository<NominaTurno, UUID> {
    List<NominaTurno> findByEmpresaClienteIdAndAnioAndSemanaOrderByTurnoAscNombreNormalizadoAsc(
            UUID empresaId, Integer anio, Integer semana);

    void deleteByEmpresaClienteIdAndAnioAndSemana(UUID empresaId, Integer anio, Integer semana);

    /** Registros de nómina de un año (y opcionalmente una semana) con el pasajero ya cargado. */
    @Query("""
            select n from NominaTurno n
            left join fetch n.pasajero
            where n.empresaCliente.id = :empresaId
              and n.anio = :anio
              and (:semana is null or n.semana = :semana)
            order by n.semana, n.turno, n.nombreNormalizado""")
    List<NominaTurno> buscarConPasajero(@Param("empresaId") UUID empresaId,
                                        @Param("anio") Integer anio,
                                        @Param("semana") Integer semana);

    /** Conteo por año/semana/turno para armar el selector de semanas del dashboard. */
    @Query("""
            select n.anio, n.semana, n.turno, count(n) from NominaTurno n
            where n.empresaCliente.id = :empresaId
            group by n.anio, n.semana, n.turno
            order by n.anio desc, n.semana desc""")
    List<Object[]> resumenSemanas(@Param("empresaId") UUID empresaId);
}
