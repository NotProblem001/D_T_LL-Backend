package com.dtll.backend.service;

import com.dtll.backend.dto.maestros.ConfiguracionResponse;
import com.dtll.backend.dto.maestros.EstadoAsistenciaRequest;
import com.dtll.backend.dto.maestros.EstadoAsistenciaResponse;
import com.dtll.backend.model.entity.Configuracion;
import com.dtll.backend.model.entity.EstadoAsistenciaConfig;
import com.dtll.backend.repository.ConfiguracionRepository;
import com.dtll.backend.repository.EstadoAsistenciaConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Parámetros de operación (clave/valor) y estados de asistencia configurables. */
@Service
@RequiredArgsConstructor
public class ConfiguracionService {

    public static final String MINUTOS_MINIMOS_ENTRE_RECORRIDOS = "MINUTOS_MINIMOS_ENTRE_RECORRIDOS";

    private final ConfiguracionRepository configuracionRepository;
    private final EstadoAsistenciaConfigRepository estadoAsistenciaRepository;

    // ------------------------------------------------------------- configuraciones

    @Transactional(readOnly = true)
    public List<ConfiguracionResponse> listarConfiguraciones() {
        return configuracionRepository.findAll().stream()
                .sorted(Comparator.comparing(Configuracion::getClave))
                .map(ConfiguracionResponse::desde)
                .toList();
    }

    @Transactional
    public ConfiguracionResponse actualizarConfiguracion(String clave, String valor) {
        Configuracion config = configuracionRepository.findById(clave)
                .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada: " + clave));
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El valor es obligatorio");
        }
        if (MINUTOS_MINIMOS_ENTRE_RECORRIDOS.equals(clave)) {
            validarEnteroPositivo(valor);
        }
        config.setValor(valor.trim());
        return ConfiguracionResponse.desde(configuracionRepository.save(config));
    }

    /** Minutos mínimos entre el término de un recorrido y el inicio del siguiente (mismo vehículo/conductor). */
    @Transactional(readOnly = true)
    public int minutosMinimosEntreRecorridos() {
        return configuracionRepository.findById(MINUTOS_MINIMOS_ENTRE_RECORRIDOS)
                .map(c -> Integer.parseInt(c.getValor()))
                .orElse(30);
    }

    // --------------------------------------------------------- estados de asistencia

    @Transactional(readOnly = true)
    public List<EstadoAsistenciaResponse> listarEstadosAsistencia() {
        return estadoAsistenciaRepository.findAllByOrderByOrdenAsc().stream()
                .map(EstadoAsistenciaResponse::desde)
                .toList();
    }

    @Transactional
    public EstadoAsistenciaResponse crearEstadoAsistencia(EstadoAsistenciaRequest request) {
        String codigo = normalizarCodigo(request.codigo());
        validarEstado(codigo, request);
        estadoAsistenciaRepository.findByCodigo(codigo).ifPresent(e -> {
            throw new IllegalStateException("Ya existe un estado de asistencia con el código " + codigo);
        });
        EstadoAsistenciaConfig estado = EstadoAsistenciaConfig.builder()
                .codigo(codigo)
                .nombre(request.nombre().trim())
                .requiereObservacion(Boolean.TRUE.equals(request.requiereObservacion()))
                .orden(request.orden() != null ? request.orden() : 0)
                .build();
        return EstadoAsistenciaResponse.desde(estadoAsistenciaRepository.save(estado));
    }

    @Transactional
    public EstadoAsistenciaResponse actualizarEstadoAsistencia(UUID id, EstadoAsistenciaRequest request) {
        EstadoAsistenciaConfig estado = obtenerEstado(id);
        String codigo = normalizarCodigo(request.codigo());
        validarEstado(codigo, request);
        estadoAsistenciaRepository.findByCodigo(codigo)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new IllegalStateException("Ya existe otro estado con el código " + codigo);
                });
        estado.setCodigo(codigo);
        estado.setNombre(request.nombre().trim());
        estado.setRequiereObservacion(Boolean.TRUE.equals(request.requiereObservacion()));
        estado.setOrden(request.orden() != null ? request.orden() : estado.getOrden());
        return EstadoAsistenciaResponse.desde(estadoAsistenciaRepository.save(estado));
    }

    @Transactional
    public EstadoAsistenciaResponse cambiarActivoEstadoAsistencia(UUID id, boolean activo) {
        EstadoAsistenciaConfig estado = obtenerEstado(id);
        estado.setActivo(activo);
        return EstadoAsistenciaResponse.desde(estadoAsistenciaRepository.save(estado));
    }

    private EstadoAsistenciaConfig obtenerEstado(UUID id) {
        return estadoAsistenciaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estado de asistencia no encontrado: " + id));
    }

    private void validarEstado(String codigo, EstadoAsistenciaRequest request) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del estado es obligatorio");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del estado es obligatorio");
        }
    }

    /** "no fue encontrado" → "NO_FUE_ENCONTRADO". */
    private String normalizarCodigo(String codigo) {
        if (codigo == null) return null;
        return codigo.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "_");
    }

    private void validarEnteroPositivo(String valor) {
        try {
            if (Integer.parseInt(valor.trim()) < 0) {
                throw new IllegalArgumentException("El valor debe ser un número mayor o igual a cero");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El valor debe ser un número entero de minutos");
        }
    }
}
