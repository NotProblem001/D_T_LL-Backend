package com.example.Report_Service.controller;

import com.example.Report_Service.model.Report;
import com.example.Report_Service.model.ReportStatus;
import com.example.Report_Service.model.ReporterType;
import com.example.Report_Service.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public ResponseEntity<Report> createReport(@RequestBody Report report) {
        return ResponseEntity.ok(reportService.createReport(report));
    }

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable String id) {
        return reportService.getReportById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Report>> getReportsByType(@PathVariable ReporterType type) {
        return ResponseEntity.ok(reportService.getReportsByType(type));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<Report>> getReportsByUser(@PathVariable String userId, @PathVariable ReporterType type) {
        return ResponseEntity.ok(reportService.getReportsByReporter(userId, type));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Report> updateStatus(@PathVariable String id, @RequestParam ReportStatus status) {
        try {
            return ResponseEntity.ok(reportService.updateReportStatus(id, status));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/metadata")
    public ResponseEntity<Report> updateMetadata(@PathVariable String id, @RequestBody Map<String, Object> metadata) {
        try {
            return ResponseEntity.ok(reportService.updateReportMetadata(id, metadata));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable String id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
