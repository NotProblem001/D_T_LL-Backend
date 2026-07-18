package com.dtll.backend.service;

import com.dtll.backend.dto.historial.*;
import com.dtll.backend.dto.incidencia.IncidenciaResponse;
import com.dtll.backend.model.entity.AsistenciaChecklist;
import com.dtll.backend.model.entity.Pasajero;
import com.dtll.backend.model.entity.Viaje;
import com.dtll.backend.model.enums.EstadoViaje;
import com.dtll.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Consulta del historial de recorridos con filtros y detalle completo (secciones 14/15). */
@Service
@RequiredArgsConstructor
public class HistorialService {

    private final ViajeRepository viajeRepository;
    private final AsistenciaChecklistRepository asistenciaRepository;
    private final AsistenciaHistorialRepository asistenciaHistorialRepository;
    private final ViajeCambioRepository viajeCambioRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final AuditoriaRepository auditoriaRepository;

    @Transactional(readOnly = true)
    public List<HistorialViajeResponse> buscar(UUID empresaId, LocalDate desde, LocalDate hasta,
                                               UUID conductorId, UUID vehiculoId, UUID rutaId,
                                               String jornada, String tipoTrayecto,
                                               EstadoViaje estado, UUID pasajeroId) {
        if (empresaId == null) {
            throw new IllegalArgumentException("La empresa es obligatoria");
        }
        LocalDate desdeFinal = desde != null ? desde : LocalDate.now().minusMonths(1);
        LocalDate hastaFinal = hasta != null ? hasta : LocalDate.now();
        return viajeRepository.buscarHistorial(empresaId, desdeFinal, hastaFinal, conductorId,
                        vehiculoId, rutaId, vacioANull(jornada), vacioANull(tipoTrayecto),
                        estado, pasajeroId).stream()
                .map(this::aFila)
                .toList();
    }

    @Transactional(readOnly = true)
    public HistorialDetalleResponse detalle(UUID viajeId) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado: " + viajeId));

        List<PasajeroHistorialResponse> pasajeros = asistenciaRepository.findByViajeId(viajeId).stream()
                .map(this::aPasajero)
                .toList();

        return new HistorialDetalleResponse(
                aFila(viaje),
                pasajeros,
                viajeCambioRepository.findByViajeIdOrderByCreatedAtAsc(viajeId).stream()
                        .map(ViajeCambioResponse::desde)
                        .toList(),
                incidenciaRepository.findByViajeIdOrderByCreatedAtDesc(viajeId).stream()
                        .map(IncidenciaResponse::desde)
                        .toList());
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> auditoria(String modulo) {
        var registros = modulo == null || modulo.isBlank()
                ? auditoriaRepository.findTop200ByOrderByCreatedAtDesc()
                : auditoriaRepository.findTop200ByModuloOrderByCreatedAtDesc(modulo);
        return registros.stream().map(AuditoriaResponse::desde).toList();
    }

    // ------------------------------------------------------------------ helpers

    /** Snapshot congelado si el viaje está cerrado; referencias actuales si sigue vigente. */
    private HistorialViajeResponse aFila(Viaje v) {
        boolean cerrado = v.getTotalPasajerosSnapshot() != null;
        long pasajeros = cerrado ? v.getTotalPasajerosSnapshot() : asistenciaRepository.countByViajeId(v.getId());

        String conductor = v.getConductorNombreSnapshot() != null ? v.getConductorNombreSnapshot()
                : v.getConductor() != null ? v.getConductor().getNombreCompleto() : null;
        String patente = v.getVehiculoPatenteSnapshot() != null ? v.getVehiculoPatenteSnapshot()
                : v.getVehiculo() != null ? v.getVehiculo().getPatente() : null;
        String ruta = v.getRutaNombreSnapshot() != null ? v.getRutaNombreSnapshot()
                : v.getRuta() != null ? v.getRuta().getNombre() : null;

        return new HistorialViajeResponse(
                v.getId(),
                v.getCodigoRutaLogin(),
                v.getFechaOperacion(),
                v.getJornadaTurno(),
                v.getTipoTrayecto(),
                v.getEstado(),
                ruta,
                conductor,
                patente,
                v.getHoraProgramadaInicio(),
                v.getHoraRealInicio(),
                v.getHoraRealTermino(),
                (int) pasajeros,
                v.getTotalTransportadosSnapshot(),
                v.getTotalAusentesSnapshot(),
                v.getTotalCancelacionesSnapshot(),
                viajeCambioRepository.findByViajeIdOrderByCreatedAtAsc(v.getId()).size(),
                incidenciaRepository.findByViajeIdOrderByCreatedAtDesc(v.getId()).size());
    }

    private PasajeroHistorialResponse aPasajero(AsistenciaChecklist a) {
        Pasajero p = a.getPasajero();
        boolean conSnapshot = a.getPasajeroNombreSnapshot() != null;
        return new PasajeroHistorialResponse(
                p.getId(),
                conSnapshot ? a.getPasajeroNombreSnapshot() : p.getNombreCompleto(),
                conSnapshot ? a.getPasajeroTelefonoSnapshot() : p.getTelefono(),
                conSnapshot ? a.getPasajeroDireccionSnapshot() : p.getDireccionReferencia(),
                a.getEstado(),
                a.getObservaciones(),
                asistenciaHistorialRepository.findByAsistenciaIdOrderByCreatedAtDesc(a.getId()).stream()
                        .map(AsistenciaHistorialResponse::desde)
                        .toList());
    }

    private String vacioANull(String v) {
        return v == null || v.isBlank() ? null : v;
    }
}
