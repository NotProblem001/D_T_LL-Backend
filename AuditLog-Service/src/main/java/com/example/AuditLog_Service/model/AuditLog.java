package com.example.AuditLog_Service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "audit_logs")
public class AuditLog {
    @Id
    private String id;
    private String userId;
    private String action; // e.g. "CREATE_BOOKING", "LOGIN"
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp = LocalDateTime.now();
}
