package com.example.Trip_Service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "dispatch-service")
public interface DispatchClient {

    @PutMapping("/dispatch/vehicle/{driverId}/status")
    void updateVehicleStatus(@PathVariable("driverId") String driverId, @RequestParam("status") String status);
}
