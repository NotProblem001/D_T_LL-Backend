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
}
