package com.dtll.backend.service;

import com.dtll.backend.dto.maestros.ComunaRequest;
import com.dtll.backend.dto.maestros.ComunaResponse;
import com.dtll.backend.dto.maestros.SectorRequest;
import com.dtll.backend.dto.maestros.SectorResponse;
import com.dtll.backend.model.entity.Comuna;
import com.dtll.backend.model.entity.Sector;
import com.dtll.backend.repository.ComunaRepository;
import com.dtll.backend.repository.SectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Maestros de geografía: comunas y sectores (agrupaciones de comunas para armar rutas). */
@Service
@RequiredArgsConstructor
public class GeografiaService {

    private final ComunaRepository comunaRepository;
    private final SectorRepository sectorRepository;

    // ------------------------------------------------------------------ comunas

    @Transactional(readOnly = true)
    public List<ComunaResponse> listarComunas() {
        return comunaRepository.findAll().stream()
                .sorted(Comparator.comparing(Comuna::getNombre))
                .map(ComunaResponse::desde)
                .toList();
    }

    @Transactional
    public ComunaResponse crearComuna(ComunaRequest request) {
        String nombre = limpiarObligatorio(request.nombre(), "El nombre de la comuna es obligatorio");
        comunaRepository.findByNombreIgnoreCase(nombre).ifPresent(c -> {
            throw new IllegalStateException("Ya existe la comuna " + nombre);
        });
        return ComunaResponse.desde(comunaRepository.save(Comuna.builder().nombre(nombre).build()));
    }

    @Transactional
    public ComunaResponse actualizarComuna(UUID id, ComunaRequest request) {
        Comuna comuna = comunaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comuna no encontrada: " + id));
        String nombre = limpiarObligatorio(request.nombre(), "El nombre de la comuna es obligatorio");
        comunaRepository.findByNombreIgnoreCase(nombre)
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> {
                    throw new IllegalStateException("Ya existe otra comuna con el nombre " + nombre);
                });
        comuna.setNombre(nombre);
        return ComunaResponse.desde(comunaRepository.save(comuna));
    }

    @Transactional
    public ComunaResponse cambiarActivoComuna(UUID id, boolean activo) {
        Comuna comuna = comunaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comuna no encontrada: " + id));
        comuna.setActivo(activo);
        return ComunaResponse.desde(comunaRepository.save(comuna));
    }

    // ------------------------------------------------------------------ sectores

    @Transactional(readOnly = true)
    public List<SectorResponse> listarSectores() {
        return sectorRepository.findAll().stream()
                .sorted(Comparator.comparing(Sector::getNombre))
                .map(SectorResponse::desde)
                .toList();
    }

    @Transactional
    public SectorResponse crearSector(SectorRequest request) {
        String nombre = limpiarObligatorio(request.nombre(), "El nombre del sector es obligatorio");
        sectorRepository.findByNombreIgnoreCase(nombre).ifPresent(s -> {
            throw new IllegalStateException("Ya existe el sector " + nombre);
        });
        Sector sector = Sector.builder().nombre(nombre).build();
        aplicarCamposSector(sector, request, nombre);
        return SectorResponse.desde(sectorRepository.save(sector));
    }

    @Transactional
    public SectorResponse actualizarSector(UUID id, SectorRequest request) {
        Sector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sector no encontrado: " + id));
        String nombre = limpiarObligatorio(request.nombre(), "El nombre del sector es obligatorio");
        sectorRepository.findByNombreIgnoreCase(nombre)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new IllegalStateException("Ya existe otro sector con el nombre " + nombre);
                });
        aplicarCamposSector(sector, request, nombre);
        return SectorResponse.desde(sectorRepository.save(sector));
    }

    @Transactional
    public SectorResponse cambiarActivoSector(UUID id, boolean activo) {
        Sector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sector no encontrado: " + id));
        sector.setActivo(activo);
        return SectorResponse.desde(sectorRepository.save(sector));
    }

    private void aplicarCamposSector(Sector sector, SectorRequest request, String nombre) {
        sector.setNombre(nombre);
        sector.setDescripcion(request.descripcion() == null || request.descripcion().isBlank()
                ? null : request.descripcion().trim());
        List<Comuna> comunas = new ArrayList<>();
        if (request.comunaIds() != null) {
            for (UUID comunaId : request.comunaIds()) {
                comunas.add(comunaRepository.findById(comunaId)
                        .orElseThrow(() -> new IllegalArgumentException("Comuna no encontrada: " + comunaId)));
            }
        }
        sector.setComunas(comunas);
    }

    private String limpiarObligatorio(String valor, String mensajeError) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor.trim();
    }
}
