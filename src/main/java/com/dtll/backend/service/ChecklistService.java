package com.dtll.backend.service;

import com.dtll.backend.dto.checklist.ChecklistItemResponse;
import com.dtll.backend.dto.checklist.ChecklistUpdateRequest;
import com.dtll.backend.model.entity.AsistenciaChecklist;
import com.dtll.backend.repository.AsistenciaChecklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final AsistenciaChecklistRepository asistenciaChecklistRepository;

    public List<ChecklistItemResponse> obtenerPorViaje(UUID viajeId) {
        return asistenciaChecklistRepository.findByViajeId(viajeId).stream()
                .map(a -> new ChecklistItemResponse(
                        a.getId(),
                        a.getPasajero().getId(),
                        a.getPasajero().getNombreCompleto(),
                        a.getPasajero().getPuntoParadaAsignado(),
                        a.getEstado()))
                .toList();
    }

    @Transactional
    public List<ChecklistItemResponse> actualizar(UUID viajeId, ChecklistUpdateRequest request) {
        Map<UUID, AsistenciaChecklist> asistenciasPorId = asistenciaChecklistRepository.findByViajeId(viajeId).stream()
                .collect(Collectors.toMap(AsistenciaChecklist::getId, a -> a));

        for (ChecklistUpdateRequest.Item item : request.marcaciones()) {
            AsistenciaChecklist asistencia = asistenciasPorId.get(item.asistenciaId());
            if (asistencia == null) {
                continue; // no pertenece a este viaje, se ignora
            }
            asistencia.setEstado(item.estado());
            asistencia.setObservaciones(item.observaciones());
            asistencia.setHoraMarcaje(LocalDateTime.now());
        }

        asistenciaChecklistRepository.saveAll(asistenciasPorId.values());
        return obtenerPorViaje(viajeId);
    }
}
