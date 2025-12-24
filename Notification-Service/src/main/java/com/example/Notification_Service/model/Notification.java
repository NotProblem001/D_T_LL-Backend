package com.example.Notification_Service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    private String recipient; // Email or Phone
    private String message;
    private NotificationType type;
    private NotificationStatus status;
    private LocalDateTime createdAt = LocalDateTime.now();
    private String bookingId; // Optional reference
}
