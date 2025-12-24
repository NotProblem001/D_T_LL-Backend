package com.example.Notification_Service.repository;

import com.example.Notification_Service.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByRecipient(String recipient);
}
