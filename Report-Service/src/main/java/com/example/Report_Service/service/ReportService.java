package com.example.Report_Service.service;

import com.example.Report_Service.model.Report;
import com.example.Report_Service.model.ReportStatus;
import com.example.Report_Service.model.ReporterType;
import com.example.Report_Service.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public Report createReport(Report report) {
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        if (report.getStatus() == null) {
            report.setStatus(ReportStatus.PENDING);
        }
        return reportRepository.save(report);
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Optional<Report> getReportById(String id) {
        return reportRepository.findById(id);
    }

    public List<Report> getReportsByReporter(String reporterId, ReporterType type) {
        return reportRepository.findByReporterIdAndReporterType(reporterId, type);
    }
    
    public List<Report> getReportsByType(ReporterType type) {
        return reportRepository.findByReporterType(type);
    }

    public Report updateReportStatus(String id, ReportStatus status) {
        return reportRepository.findById(id)
                .map(report -> {
                    report.setStatus(status);
                    report.setUpdatedAt(LocalDateTime.now());
                    return reportRepository.save(report);
                })
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
    }

    public Report updateReportMetadata(String id, Map<String, Object> metadata) {
        return reportRepository.findById(id)
                .map(report -> {
                    if (report.getMetadata() == null) {
                        report.setMetadata(metadata);
                    } else {
                        report.getMetadata().putAll(metadata);
                    }
                    report.setUpdatedAt(LocalDateTime.now());
                    return reportRepository.save(report);
                })
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
    }
    
    public void deleteReport(String id) {
        reportRepository.deleteById(id);
    }
}
