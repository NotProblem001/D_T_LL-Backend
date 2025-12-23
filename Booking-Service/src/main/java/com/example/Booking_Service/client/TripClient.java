package com.example.Booking_Service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "trip-service")
public interface TripClient {

    @PostMapping("/trips/{id}/reserve")
    boolean reserveSeats(@PathVariable("id") String id, @RequestParam("seats") int seats);
}
