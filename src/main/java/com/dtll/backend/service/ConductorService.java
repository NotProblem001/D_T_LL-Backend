package com.dtll.backend.service;

import com.dtll.backend.dto.maestros.ConductorRequest;
import com.dtll.backend.dto.maestros.ConductorResponse;
import com.dtll.backend.model.entity.Conductor;
import com.dtll.backend.repository.ConductorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * CRUD de conductores. El PIN (login RUT+PIN de la app del conductor) se guarda
 * hasheado con BCrypt, igual que en AuthService.
 */
@Service
@RequiredArgsConstructor
public class ConductorService {

    private final ConductorRepository conductorRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<ConductorResponse> listar() {
        return conductorRepository.findAll().stream()
                .sorted(Comparator.comparing(Conductor::getNombreCompleto))
                .map(ConductorResponse::desde)
                .toList();
    }

    @Transactional
    public ConductorResponse crear(ConductorRequest request) {
        String rut = normalizarRut(request.rutConductor());
        validar(rut, request);
        conductorRepository.findByRutConductor(rut).ifPresent(c -> {
            throw new IllegalStateException("Ya existe un conductor con el RUT " + rut);
        });

        Conductor conductor = Conductor.builder().rutConductor(rut).build();
        aplicarCampos(conductor, request);
        aplicarPin(conductor, request.pin());
        return ConductorResponse.desde(conductorRepository.save(conductor));
    }

    @Transactional
    public ConductorResponse actualizar(UUID id, ConductorRequest request) {
        Conductor conductor = obtener(id);
        String rut = normalizarRut(request.rutConductor());
        validar(rut, request);
        conductorRepository.findByRutConductor(rut)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new IllegalStateException("Ya existe otro conductor con el RUT " + rut);
                });

        conductor.setRutConductor(rut);
        aplicarCampos(conductor, request);
        aplicarPin(conductor, request.pin());
        return ConductorResponse.desde(conductorRepository.save(conductor));
    }

    @Transactional
    public ConductorResponse cambiarActivo(UUID id, boolean activo) {
        Conductor conductor = obtener(id);
        conductor.setActivo(activo);
        return ConductorResponse.desde(conductorRepository.save(conductor));
    }

    private Conductor obtener(UUID id) {
        return conductorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conductor no encontrado: " + id));
    }

    private void aplicarCampos(Conductor conductor, ConductorRequest request) {
        conductor.setNombreCompleto(request.nombreCompleto().trim());
        conductor.setTelefono(limpiar(request.telefono()));
        conductor.setEmail(limpiar(request.email()));
        conductor.setTipoContrato(request.tipoContrato());
        conductor.setTarifaPorViaje(request.tarifaPorViaje());
        conductor.setTipoLicencia(limpiar(request.tipoLicencia()));
        conductor.setFechaVencimientoLicencia(request.fechaVencimientoLicencia());
        conductor.setObservaciones(limpiar(request.observaciones()));
    }

    /** PIN vacío en edición = conservar el actual; con valor = validar 4-6 dígitos y re-hashear. */
    private void aplicarPin(Conductor conductor, String pin) {
        if (pin == null || pin.isBlank()) return;
        String limpio = pin.trim();
        if (!limpio.matches("\\d{4,6}")) {
            throw new IllegalArgumentException("El PIN debe tener entre 4 y 6 dígitos");
        }
        conductor.setPinAccesoHash(passwordEncoder.encode(limpio));
    }

    private void validar(String rut, ConductorRequest request) {
        if (rut == null || rut.isBlank()) {
            throw new IllegalArgumentException("El RUT del conductor es obligatorio");
        }
        if (request.nombreCompleto() == null || request.nombreCompleto().isBlank()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }
    }

    /** Mismo formato canónico que usa el login RUT+PIN (ver RutUtil). */
    private String normalizarRut(String rut) {
        return com.dtll.backend.util.RutUtil.normalizar(rut);
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
