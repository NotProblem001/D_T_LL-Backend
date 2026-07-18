package com.dtll.backend.repository;

import com.dtll.backend.model.entity.Viaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface ViajeRepository extends JpaRepository<Viaje, UUID> {
    boolean existsByCodigoRutaLogin(String codigoRutaLogin);
    long countByEmpresaClienteIdAndFechaOperacionBetween(UUID empresaId, LocalDate desde, LocalDate hasta);

    /** Agrupación de la importación Excel: un viaje por conductor+empresa+fecha+jornada+trayecto. */
    java.util.Optional<Viaje> findByEmpresaClienteIdAndConductorIdAndFechaOperacionAndJornadaTurnoAndTipoTrayecto(
            UUID empresaId, UUID conductorId, LocalDate fechaOperacion, String jornadaTurno, String tipoTrayecto);

    // --- Planificación (Etapa 3) ---

    java.util.List<Viaje> findByEmpresaClienteIdAndFechaOperacionOrderByJornadaTurnoAscTipoTrayectoAsc(
            UUID empresaId, LocalDate fechaOperacion);

    /** Evita regenerar un viaje ya propuesto para la misma ruta/turno/trayecto del día. */
    boolean existsByEmpresaClienteIdAndFechaOperacionAndJornadaTurnoAndTipoTrayectoAndRutaIdAndEstadoNot(
            UUID empresaId, LocalDate fechaOperacion, String jornadaTurno, String tipoTrayecto,
            UUID rutaId, com.dtll.backend.model.enums.EstadoViaje estadoExcluido);

    java.util.List<Viaje> findByConductorIdAndFechaOperacion(UUID conductorId, LocalDate fechaOperacion);

    java.util.List<Viaje> findByVehiculoIdAndFechaOperacion(UUID vehiculoId, LocalDate fechaOperacion);

    /** Búsqueda del historial de recorridos con filtros combinables (sección 15). */
    @org.springframework.data.jpa.repository.Query("""
            select v from Viaje v
            left join v.conductor c
            left join v.vehiculo veh
            left join v.ruta r
            where v.empresaCliente.id = :empresaId
              and v.fechaOperacion between :desde and :hasta
              and (:conductorId is null or c.id = :conductorId)
              and (:vehiculoId is null or veh.id = :vehiculoId)
              and (:rutaId is null or r.id = :rutaId)
              and (:jornada is null or v.jornadaTurno = :jornada)
              and (:tipoTrayecto is null or v.tipoTrayecto = :tipoTrayecto)
              and (:estado is null or v.estado = :estado)
              and (:pasajeroId is null or exists (
                    select 1 from AsistenciaChecklist a
                    where a.viaje = v and a.pasajero.id = :pasajeroId))
            order by v.fechaOperacion desc, v.horaProgramadaInicio desc""")
    java.util.List<Viaje> buscarHistorial(
            @org.springframework.data.repository.query.Param("empresaId") UUID empresaId,
            @org.springframework.data.repository.query.Param("desde") LocalDate desde,
            @org.springframework.data.repository.query.Param("hasta") LocalDate hasta,
            @org.springframework.data.repository.query.Param("conductorId") UUID conductorId,
            @org.springframework.data.repository.query.Param("vehiculoId") UUID vehiculoId,
            @org.springframework.data.repository.query.Param("rutaId") UUID rutaId,
            @org.springframework.data.repository.query.Param("jornada") String jornada,
            @org.springframework.data.repository.query.Param("tipoTrayecto") String tipoTrayecto,
            @org.springframework.data.repository.query.Param("estado") com.dtll.backend.model.enums.EstadoViaje estado,
            @org.springframework.data.repository.query.Param("pasajeroId") UUID pasajeroId);
}
