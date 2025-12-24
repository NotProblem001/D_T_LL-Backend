package com.example.AuditLog_Service.repository;

import com.example.AuditLog_Service.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByUserId(String userId);
}
