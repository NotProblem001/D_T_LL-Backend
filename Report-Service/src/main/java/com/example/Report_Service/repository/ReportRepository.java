package com.example.Report_Service.repository;

import com.example.Report_Service.model.Report;
import com.example.Report_Service.model.ReporterType;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReportRepository extends MongoRepository<Report, String> {
    List<Report> findByReporterId(String reporterId);
    List<Report> findByReporterType(ReporterType reporterType);
    List<Report> findByReporterIdAndReporterType(String reporterId, ReporterType reporterType);
}
