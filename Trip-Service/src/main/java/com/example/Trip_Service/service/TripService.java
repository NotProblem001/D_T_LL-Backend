package com.example.Trip_Service.service;

import com.example.Trip_Service.model.Trip;
import com.example.Trip_Service.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TripService {

    @Autowired
    private TripRepository repository;

    public Trip saveTrip(Trip trip) {
        trip.setStatus("scheduled");
        return repository.save(trip);
    }

    public List<Trip> getAllTrips() {
        return repository.findAll();
    }

    public Trip getTripById(String id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Trip not found"));
    }

    public boolean reserveSeats(String tripId, int seats) {
        Trip trip = getTripById(tripId);
        if (trip.getAvailableSeats() >= seats) {
            trip.setAvailableSeats(trip.getAvailableSeats() - seats);
            repository.save(trip);
            return true;
        }
        return false;
    }
}
