package com.example.Report_Service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "reports")
public class Report {
    @Id
    private String id;
    
    private String reporterId;
    private ReporterType reporterType;
    
    private String subject;
    private String description;
    
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Dynamic field for extra data (location, image urls, specific IDs, etc.)
    private Map<String, Object> metadata;
}
