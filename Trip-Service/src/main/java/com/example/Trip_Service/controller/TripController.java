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
        return service.startTrip(trip);
    }

    @GetMapping
    public List<Trip> getAllTrips() {
        return service.getAllTrips();
    }

    @GetMapping("/{id}")
    public Trip getTrip(@PathVariable String id) {
        return service.getTripById(id);
    }

    @PostMapping("/start")
    public Trip startTrip(@RequestBody Trip trip) {
        return service.startTrip(trip);
    }

    @PostMapping("/{id}/end")
    public Trip endTrip(@PathVariable String id) {
        return service.endTrip(id);
    }
}
