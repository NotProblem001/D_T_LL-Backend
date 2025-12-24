package com.example.Booking_Service.service;

import com.example.Booking_Service.client.DispatchClient;
import com.example.Booking_Service.client.DispatchResponse;
import com.example.Booking_Service.client.NotificationClient;
import com.example.Booking_Service.client.NotificationRequest;
import com.example.Booking_Service.model.Booking;
import com.example.Booking_Service.model.BookingStatus;
import com.example.Booking_Service.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository repository;

    @Autowired
    private DispatchClient dispatchClient;

    @Autowired
    private NotificationClient notificationClient;

    public Booking createBooking(Booking booking) {
        booking.setCreatedAt(LocalDateTime.now());
        booking.setStatus(BookingStatus.PENDING);
        Booking savedBooking = repository.save(booking);

        // Call Dispatch Service to find a driver
        try {
            DispatchResponse response = dispatchClient.requestDispatch(savedBooking.getId());
            if (response != null && "ASSIGNED".equals(response.getStatus())) {
                savedBooking.setDriverId(response.getDriverId());
                savedBooking.setStatus(BookingStatus.ASSIGNED);

                // Send Notification
                try {
                    NotificationRequest notif = new NotificationRequest();
                    notif.setRecipient("passenger@example.com"); // Placeholder, ideally from User Service
                    notif.setMessage("Driver assigned! Driver ID: " + response.getDriverId());
                    notif.setType("SMS");
                    notif.setBookingId(savedBooking.getId());
                    notificationClient.sendNotification(notif);
                } catch (Exception e) {
                    System.err.println("Failed to send notification: " + e.getMessage());
                }
            } else {
                // Keep as PENDING if no driver found immediately (or handle logic for
                // searching)
                // For MVP, we can leave as PENDING or cancel
            }
        } catch (Exception e) {
            // Log error, keep booking as PENDING but maybe mark for retry
            System.err.println("Error calling dispatch service: " + e.getMessage());
        }

        return repository.save(savedBooking);
    }

    public List<Booking> getAllBookings() {
        return repository.findAll();
    }
}
