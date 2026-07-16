package com.dtll.backend.service;

import com.dtll.backend.dto.nomina.NominaRegistroResponse;
import com.dtll.backend.dto.nomina.NominaSemanaResumenResponse;
import com.dtll.backend.repository.NominaTurnoRepository;
import com.dtll.backend.util.Normalizador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** Consultas con filtros sobre la nómina importada, para el dashboard del Admin. */
@Service
@RequiredArgsConstructor
public class NominaConsultaService {

    private final NominaTurnoRepository nominaTurnoRepository;

    @Transactional(readOnly = true)
    public List<NominaRegistroResponse> listar(UUID empresaId, int anio, Integer semana,
                                               String turno, String comuna, String busqueda) {
        String turnoNorm = normalizarFiltro(turno);
        String comunaNorm = normalizarFiltro(comuna);
        String busquedaNorm = normalizarFiltro(busqueda);

        return nominaTurnoRepository.buscarConPasajero(empresaId, anio, semana).stream()
                .map(NominaRegistroResponse::desde)
                .filter(r -> turnoNorm == null || turnoNorm.equals(r.turno()))
                .filter(r -> comunaNorm == null
                        || (r.comuna() != null && Normalizador.nombre(r.comuna()).equals(comunaNorm)))
                .filter(r -> busquedaNorm == null || coincide(r, busquedaNorm))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NominaSemanaResumenResponse> semanas(UUID empresaId) {
        // clave "anio-semana" → conteos por turno, preservando el orden (desc) de la query.
        Map<String, Map<String, Long>> porSemana = new LinkedHashMap<>();
        for (Object[] fila : nominaTurnoRepository.resumenSemanas(empresaId)) {
            String clave = fila[0] + "-" + fila[1];
            porSemana.computeIfAbsent(clave, k -> new LinkedHashMap<>())
                    .put((String) fila[2], (Long) fila[3]);
        }
        return porSemana.entrySet().stream()
                .map(e -> {
                    String[] partes = e.getKey().split("-");
                    long total = e.getValue().values().stream().mapToLong(Long::longValue).sum();
                    return new NominaSemanaResumenResponse(
                            Integer.parseInt(partes[0]), Integer.parseInt(partes[1]), total, e.getValue());
                })
                .toList();
    }

    private boolean coincide(NominaRegistroResponse r, String busquedaNorm) {
        return Normalizador.nombre(r.nombre()).contains(busquedaNorm)
                || (r.direccion() != null && Normalizador.nombre(r.direccion()).contains(busquedaNorm))
                || (r.telefono() != null && r.telefono().contains(busquedaNorm))
                || (r.centroCosto() != null && Normalizador.nombre(r.centroCosto()).contains(busquedaNorm))
                || (r.cargo() != null && Normalizador.nombre(r.cargo()).contains(busquedaNorm));
    }

    private String normalizarFiltro(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return Normalizador.nombre(valor);
    }
}
