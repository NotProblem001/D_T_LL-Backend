package com.example.Dispatch_Service.controller;

import com.example.Dispatch_Service.model.Dispatch;
import com.example.Dispatch_Service.service.DispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dispatch")
public class DispatchController {

    @Autowired
    private DispatchService service;

    @PostMapping("/request")
    public Dispatch requestDispatch(@RequestParam("bookingId") String bookingId) {
        return service.dispatchBooking(bookingId);
    }

    @PutMapping("/vehicle/{driverId}/status")
    public void updateVehicleStatus(@PathVariable("driverId") String driverId, @RequestParam("status") String status) {
        service.updateVehicleStatus(driverId, status);
    }
}
