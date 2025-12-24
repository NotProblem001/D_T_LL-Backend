package com.example.Booking_Service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "dispatch-service")
public interface DispatchClient {

    @PostMapping("/dispatch/request")
    DispatchResponse requestDispatch(@RequestParam("bookingId") String bookingId);
}
