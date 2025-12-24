package com.example.Booking_Service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    private String recipient;
    private String message;
    private String type; // EMAIL, SMS
    private String bookingId;
}
