package com.dtll.backend.service;

import com.dtll.backend.model.entity.AsistenciaChecklist;
import com.dtll.backend.model.entity.Pasajero;
import com.dtll.backend.model.entity.Viaje;
import com.dtll.backend.repository.AsistenciaChecklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Congela el snapshot de un recorrido al cierre (finalizado o cancelado):
 * conductor, vehículo, ruta, totales y datos de contacto de cada pasajero.
 * El historial conserva lo que existía al momento del servicio (sección 14).
 */
@Service
@RequiredArgsConstructor
public class ViajeSnapshotService {

    private final AsistenciaChecklistRepository asistenciaRepository;

    /** Debe llamarse dentro de la transacción que cierra el viaje; no persiste el viaje. */
    public void congelar(Viaje viaje) {
        if (viaje.getConductor() != null) {
            viaje.setConductorNombreSnapshot(viaje.getConductor().getNombreCompleto());
            viaje.setConductorRutSnapshot(viaje.getConductor().getRutConductor());
        }
        if (viaje.getVehiculo() != null) {
            viaje.setVehiculoPatenteSnapshot(viaje.getVehiculo().getPatente());
            viaje.setVehiculoCapacidadSnapshot(viaje.getVehiculo().getCapacidadPasajeros());
        }
        if (viaje.getRuta() != null) {
            viaje.setRutaNombreSnapshot(viaje.getRuta().getNombre());
        }

        List<AsistenciaChecklist> asistencias = asistenciaRepository.findByViajeId(viaje.getId());
        int transportados = 0;
        int ausentes = 0;
        int cancelaciones = 0;
        for (AsistenciaChecklist a : asistencias) {
            Pasajero p = a.getPasajero();
            a.setPasajeroNombreSnapshot(p.getNombreCompleto());
            a.setPasajeroTelefonoSnapshot(p.getTelefono());
            a.setPasajeroDireccionSnapshot(p.getDireccionReferencia());
            switch (a.getEstado() == null ? "" : a.getEstado()) {
                case "ASISTIO" -> transportados++;
                case "NO_ASISTIO", "NO_FUE_ENCONTRADO" -> ausentes++;
                case "AVISO_PREVIO" -> cancelaciones++;
                default -> { }
            }
        }
        asistenciaRepository.saveAll(asistencias);

        viaje.setTotalPasajerosSnapshot(asistencias.size());
        viaje.setTotalTransportadosSnapshot(transportados);
        viaje.setTotalAusentesSnapshot(ausentes);
        viaje.setTotalCancelacionesSnapshot(cancelaciones);
    }
}
