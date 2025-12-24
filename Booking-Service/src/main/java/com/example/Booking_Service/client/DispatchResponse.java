package com.example.Booking_Service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DispatchResponse {
    private String id;
    private String bookingId;
    private String vehicleId;
    private String driverId;
    private String status;
}
