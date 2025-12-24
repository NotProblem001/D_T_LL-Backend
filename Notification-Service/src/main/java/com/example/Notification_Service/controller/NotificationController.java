package com.example.Notification_Service.controller;

import com.example.Notification_Service.model.Notification;
import com.example.Notification_Service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService service;

    @PostMapping("/send")
    public Notification sendNotification(@RequestBody Notification notification) {
        return service.sendNotification(notification);
    }

    @GetMapping
    public List<Notification> getAllNotifications() {
        return service.getAllNotifications();
    }
}
