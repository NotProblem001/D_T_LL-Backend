package com.dtll.backend.controller;

import com.dtll.backend.dto.checklist.ChecklistItemResponse;
import com.dtll.backend.dto.checklist.ChecklistUpdateRequest;
import com.dtll.backend.service.ChecklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/checklist")
@RequiredArgsConstructor
public class ChecklistController {

    private final ChecklistService checklistService;

    @GetMapping("/{viajeId}")
    public ResponseEntity<List<ChecklistItemResponse>> obtener(@PathVariable UUID viajeId) {
        return ResponseEntity.ok(checklistService.obtenerPorViaje(viajeId));
    }

    @PutMapping("/{viajeId}")
    public ResponseEntity<List<ChecklistItemResponse>> actualizar(@PathVariable UUID viajeId,
                                                                    @RequestBody ChecklistUpdateRequest request) {
        return ResponseEntity.ok(checklistService.actualizar(viajeId, request));
    }
}
