package com.dtll.backend.service;

import com.dtll.backend.dto.maestros.VehiculoRequest;
import com.dtll.backend.dto.maestros.VehiculoResponse;
import com.dtll.backend.model.entity.Conductor;
import com.dtll.backend.model.entity.Vehiculo;
import com.dtll.backend.model.enums.EstadoVehiculo;
import com.dtll.backend.repository.ConductorRepository;
import com.dtll.backend.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** CRUD de la flota para el módulo de Vehículos (maestros, solo ADMIN edita). */
@Service
@RequiredArgsConstructor
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;

    @Transactional(readOnly = true)
    public List<VehiculoResponse> listar() {
        return vehiculoRepository.findAll().stream()
                .sorted(Comparator.comparing(Vehiculo::getPatente))
                .map(VehiculoResponse::desde)
                .toList();
    }

    @Transactional
    public VehiculoResponse crear(VehiculoRequest request) {
        String patente = normalizarPatente(request.patente());
        validar(patente, request);
        vehiculoRepository.findByPatente(patente).ifPresent(v -> {
            throw new IllegalStateException("Ya existe un vehículo con la patente " + patente);
        });

        Vehiculo vehiculo = Vehiculo.builder().patente(patente).capacidadPasajeros(request.capacidadPasajeros()).build();
        aplicarCampos(vehiculo, request);
        return VehiculoResponse.desde(vehiculoRepository.save(vehiculo));
    }

    @Transactional
    public VehiculoResponse actualizar(UUID id, VehiculoRequest request) {
        Vehiculo vehiculo = obtener(id);
        String patente = normalizarPatente(request.patente());
        validar(patente, request);
        vehiculoRepository.findByPatente(patente)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new IllegalStateException("Ya existe otro vehículo con la patente " + patente);
                });

        vehiculo.setPatente(patente);
        aplicarCampos(vehiculo, request);
        return VehiculoResponse.desde(vehiculoRepository.save(vehiculo));
    }

    @Transactional
    public VehiculoResponse cambiarActivo(UUID id, boolean activo) {
        Vehiculo vehiculo = obtener(id);
        vehiculo.setActivo(activo);
        return VehiculoResponse.desde(vehiculoRepository.save(vehiculo));
    }

    private Vehiculo obtener(UUID id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado: " + id));
    }

    private void aplicarCampos(Vehiculo vehiculo, VehiculoRequest request) {
        vehiculo.setMarca(limpiar(request.marca()));
        vehiculo.setModelo(limpiar(request.modelo()));
        vehiculo.setAnio(request.anio());
        vehiculo.setCapacidadPasajeros(request.capacidadPasajeros());
        vehiculo.setTipoVehiculo(limpiar(request.tipoVehiculo()));
        vehiculo.setEstado(request.estado() != null ? request.estado() : EstadoVehiculo.DISPONIBLE);
        vehiculo.setKilometraje(request.kilometraje());
        vehiculo.setFechaRevisionTecnica(request.fechaRevisionTecnica());
        vehiculo.setFechaPermisoCirculacion(request.fechaPermisoCirculacion());
        vehiculo.setFechaVencimientoSeguro(request.fechaVencimientoSeguro());
        vehiculo.setObservaciones(limpiar(request.observaciones()));

        if (request.conductorHabitualId() != null) {
            Conductor conductor = conductorRepository.findById(request.conductorHabitualId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Conductor no encontrado: " + request.conductorHabitualId()));
            vehiculo.setConductorHabitual(conductor);
        } else {
            vehiculo.setConductorHabitual(null);
        }
    }

    private void validar(String patente, VehiculoRequest request) {
        if (patente == null || patente.isBlank()) {
            throw new IllegalArgumentException("La patente es obligatoria");
        }
        if (request.capacidadPasajeros() == null || request.capacidadPasajeros() <= 0) {
            throw new IllegalArgumentException("La capacidad de pasajeros debe ser mayor a cero");
        }
    }

    /** Normaliza la patente a mayúsculas sin espacios ni puntos, ej: JKLM12. */
    private String normalizarPatente(String patente) {
        if (patente == null) return null;
        return patente.replace(".", "").replace(" ", "").replace("-", "").toUpperCase();
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
