package com.example.Trip_Service.controller;

import com.example.Trip_Service.model.Trip;
import com.example.Trip_Service.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private TripService service;

    @PostMapping
    public Trip createTrip(@RequestBody Trip trip) {
        return service.saveTrip(trip);
    }

    @GetMapping
    public List<Trip> getAllTrips() {
        return service.getAllTrips();
    }

    @GetMapping("/{id}")
    public Trip getTrip(@PathVariable String id) {
        return service.getTripById(id);
    }

    @PostMapping("/{id}/reserve")
    public boolean reserveSeats(@PathVariable String id, @RequestParam int seats) {
        return service.reserveSeats(id, seats);
    }
}
