package com.example.Notification_Service.service;

import com.example.Notification_Service.model.Notification;
import com.example.Notification_Service.model.NotificationStatus;
import com.example.Notification_Service.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository repository;

    public Notification sendNotification(Notification notification) {
        // Mock sending logic (e.g., email or push)
        System.out.println("Sending notification to: " + notification.getRecipient());
        System.out.println("Message: " + notification.getMessage());

        notification.setStatus(NotificationStatus.SENT);
        return repository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        return repository.findAll();
    }
}
