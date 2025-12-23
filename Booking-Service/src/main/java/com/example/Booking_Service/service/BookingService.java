package com.example.Booking_Service.service;

import com.example.Booking_Service.client.TripClient;
import com.example.Booking_Service.model.Booking;
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
    private TripClient tripClient;

    public Booking createBooking(Booking booking) {
        // 1. Reserve seats in Trip Service
        boolean reserved = tripClient.reserveSeats(booking.getTripId(), booking.getSeats());

        if (reserved) {
            booking.setBookingTime(LocalDateTime.now());
            booking.setStatus("CONFIRMED");
            return repository.save(booking);
        } else {
            throw new RuntimeException("Failed to reserve seats: Not enough availability or trip not found");
        }
    }

    public List<Booking> getAllBookings() {
        return repository.findAll();
    }
}
