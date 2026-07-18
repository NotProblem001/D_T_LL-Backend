package com.dtll.backend.service;

import com.dtll.backend.dto.maestros.TurnoRequest;
import com.dtll.backend.dto.maestros.TurnoResponse;
import com.dtll.backend.model.entity.Turno;
import com.dtll.backend.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Maestro de turnos configurables (nombre + entrada/salida + horas + días). */
@Service
@RequiredArgsConstructor
public class TurnoService {

    private static final Set<String> DIAS_VALIDOS = Set.of("LU", "MA", "MI", "JU", "VI", "SA", "DO");

    private final TurnoRepository turnoRepository;

    @Transactional(readOnly = true)
    public List<TurnoResponse> listar() {
        return turnoRepository.findAll().stream()
                .sorted(Comparator.comparing(Turno::getHoraInicio)
                        .thenComparing(Turno::getNombre))
                .map(TurnoResponse::desde)
                .toList();
    }

    @Transactional
    public TurnoResponse crear(TurnoRequest request) {
        validar(request);
        String nombre = request.nombre().trim();
        turnoRepository.findByNombreIgnoreCaseAndTipoServicio(nombre, request.tipoServicio())
                .ifPresent(t -> {
                    throw new IllegalStateException("Ya existe el turno " + nombre
                            + " (" + request.tipoServicio() + ")");
                });
        Turno turno = new Turno();
        aplicarCampos(turno, request);
        return TurnoResponse.desde(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoResponse actualizar(UUID id, TurnoRequest request) {
        Turno turno = obtener(id);
        validar(request);
        String nombre = request.nombre().trim();
        turnoRepository.findByNombreIgnoreCaseAndTipoServicio(nombre, request.tipoServicio())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new IllegalStateException("Ya existe otro turno " + nombre
                            + " (" + request.tipoServicio() + ")");
                });
        aplicarCampos(turno, request);
        return TurnoResponse.desde(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoResponse cambiarActivo(UUID id, boolean activo) {
        Turno turno = obtener(id);
        turno.setActivo(activo);
        return TurnoResponse.desde(turnoRepository.save(turno));
    }

    private Turno obtener(UUID id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado: " + id));
    }

    private void aplicarCampos(Turno turno, TurnoRequest request) {
        turno.setNombre(request.nombre().trim());
        turno.setTipoServicio(request.tipoServicio());
        turno.setHoraInicio(request.horaInicio());
        turno.setHoraLlegadaEstimada(request.horaLlegadaEstimada());
        turno.setDiasSemana(normalizarDias(request.diasSemana()));
    }

    private void validar(TurnoRequest request) {
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del turno es obligatorio");
        }
        if (request.tipoServicio() == null) {
            throw new IllegalArgumentException("El tipo de servicio (ENTRADA/SALIDA) es obligatorio");
        }
        if (request.horaInicio() == null) {
            throw new IllegalArgumentException("La hora de inicio es obligatoria");
        }
    }

    /** Valida y normaliza los días: "lu, ma" → "LU,MA". */
    private String normalizarDias(String dias) {
        if (dias == null || dias.isBlank()) return null;
        StringBuilder normalizado = new StringBuilder();
        for (String dia : dias.split(",")) {
            String codigo = dia.trim().toUpperCase(Locale.ROOT);
            if (codigo.isEmpty()) continue;
            if (!DIAS_VALIDOS.contains(codigo)) {
                throw new IllegalArgumentException(
                        "Día inválido: " + dia.trim() + " (use LU, MA, MI, JU, VI, SA, DO)");
            }
            if (normalizado.length() > 0) normalizado.append(",");
            normalizado.append(codigo);
        }
        return normalizado.length() == 0 ? null : normalizado.toString();
    }
}
