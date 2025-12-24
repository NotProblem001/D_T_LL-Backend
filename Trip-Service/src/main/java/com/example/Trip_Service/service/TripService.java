package com.example.Trip_Service.service;

import com.example.Trip_Service.model.Trip;
import com.example.Trip_Service.model.TripStatus;
import com.example.Trip_Service.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Trip_Service.client.DispatchClient;
import java.util.List;
import java.util.UUID;

@Service
public class TripService {

    @Autowired
    private TripRepository repository;

    @Autowired
    private DispatchClient dispatchClient;

    public Trip startTrip(Trip trip) {
        trip.setStatus(TripStatus.STARTED);
        trip.setStartTime(java.time.LocalDateTime.now());
        return repository.save(trip);
    }

    public Trip endTrip(String id) {
        Trip trip = getTripById(id);
        trip.setStatus(TripStatus.ENDED);
        trip.setEndTime(java.time.LocalDateTime.now());

        // Release driver
        try {
            dispatchClient.updateVehicleStatus(trip.getDriverId(), "AVAILABLE");
        } catch (Exception e) {
            System.err.println("Failed to release driver: " + e.getMessage());
        }

        return repository.save(trip);
    }

    public List<Trip> getAllTrips() {
        return repository.findAll();
    }

    public Trip getTripById(String id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Trip not found"));
    }
}
