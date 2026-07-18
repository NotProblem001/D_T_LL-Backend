package com.dtll.backend.service;

import com.dtll.backend.dto.maestros.RutaRequest;
import com.dtll.backend.dto.maestros.RutaResponse;
import com.dtll.backend.model.entity.Ruta;
import com.dtll.backend.model.entity.Sector;
import com.dtll.backend.repository.ConductorRepository;
import com.dtll.backend.repository.EmpresaClienteRepository;
import com.dtll.backend.repository.RutaRepository;
import com.dtll.backend.repository.SectorRepository;
import com.dtll.backend.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Maestro de rutas: plantillas por empresa que agrupan sectores, con habituales referenciales. */
@Service
@RequiredArgsConstructor
public class RutaService {

    private final RutaRepository rutaRepository;
    private final EmpresaClienteRepository empresaClienteRepository;
    private final SectorRepository sectorRepository;
    private final ConductorRepository conductorRepository;
    private final VehiculoRepository vehiculoRepository;

    @Transactional(readOnly = true)
    public List<RutaResponse> listar(UUID empresaId) {
        List<Ruta> rutas = empresaId != null
                ? rutaRepository.findByEmpresaClienteId(empresaId)
                : rutaRepository.findAll();
        return rutas.stream()
                .sorted(Comparator.comparing(Ruta::getNombre))
                .map(RutaResponse::desde)
                .toList();
    }

    @Transactional
    public RutaResponse crear(RutaRequest request) {
        validar(request);
        rutaRepository.findByEmpresaClienteIdAndNombreIgnoreCase(request.empresaId(), request.nombre().trim())
                .ifPresent(r -> {
                    throw new IllegalStateException(
                            "Ya existe una ruta con el nombre " + request.nombre().trim() + " para esa empresa");
                });
        Ruta ruta = new Ruta();
        aplicarCampos(ruta, request);
        return RutaResponse.desde(rutaRepository.save(ruta));
    }

    @Transactional
    public RutaResponse actualizar(UUID id, RutaRequest request) {
        Ruta ruta = obtener(id);
        validar(request);
        rutaRepository.findByEmpresaClienteIdAndNombreIgnoreCase(request.empresaId(), request.nombre().trim())
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> {
                    throw new IllegalStateException(
                            "Ya existe otra ruta con el nombre " + request.nombre().trim() + " para esa empresa");
                });
        aplicarCampos(ruta, request);
        return RutaResponse.desde(rutaRepository.save(ruta));
    }

    @Transactional
    public RutaResponse cambiarActivo(UUID id, boolean activo) {
        Ruta ruta = obtener(id);
        ruta.setActivo(activo);
        return RutaResponse.desde(rutaRepository.save(ruta));
    }

    private Ruta obtener(UUID id) {
        return rutaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + id));
    }

    private void aplicarCampos(Ruta ruta, RutaRequest request) {
        ruta.setEmpresaCliente(empresaClienteRepository.findById(request.empresaId())
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada: " + request.empresaId())));
        ruta.setNombre(request.nombre().trim());
        ruta.setDescripcion(request.descripcion() == null || request.descripcion().isBlank()
                ? null : request.descripcion().trim());

        List<Sector> sectores = new ArrayList<>();
        if (request.sectorIds() != null) {
            for (UUID sectorId : request.sectorIds()) {
                sectores.add(sectorRepository.findById(sectorId)
                        .orElseThrow(() -> new IllegalArgumentException("Sector no encontrado: " + sectorId)));
            }
        }
        ruta.setSectores(sectores);

        ruta.setConductorHabitual(request.conductorHabitualId() == null ? null
                : conductorRepository.findById(request.conductorHabitualId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Conductor no encontrado: " + request.conductorHabitualId())));
        ruta.setVehiculoHabitual(request.vehiculoHabitualId() == null ? null
                : vehiculoRepository.findById(request.vehiculoHabitualId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Vehículo no encontrado: " + request.vehiculoHabitualId())));
    }

    private void validar(RutaRequest request) {
        if (request.empresaId() == null) {
            throw new IllegalArgumentException("La empresa es obligatoria");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la ruta es obligatorio");
        }
    }
}
