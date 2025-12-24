package com.example.Dispatch_Service.service;

import com.example.Dispatch_Service.model.Dispatch;
import com.example.Dispatch_Service.model.DispatchStatus;
import com.example.Dispatch_Service.model.Vehicle;
import com.example.Dispatch_Service.model.VehicleStatus;
import com.example.Dispatch_Service.repository.DispatchRepository;
import com.example.Dispatch_Service.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DispatchService {

    @Autowired
    private DispatchRepository dispatchRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    public Dispatch dispatchBooking(String bookingId) {
        // 1. Find available vehicle (Simplified logic: find any available)
        Optional<Vehicle> availableVehicle = vehicleRepository.findFirstByStatus(VehicleStatus.AVAILABLE);

        Dispatch dispatch = new Dispatch();
        dispatch.setBookingId(bookingId);

        if (availableVehicle.isPresent()) {
            Vehicle vehicle = availableVehicle.get();
            dispatch.setVehicleId(vehicle.getId());
            dispatch.setDriverId(vehicle.getDriverId());
            dispatch.setStatus(DispatchStatus.ASSIGNED);

            // Mark vehicle as BUSY
            vehicle.setStatus(VehicleStatus.BUSY);
            vehicleRepository.save(vehicle);
        } else {
            dispatch.setStatus(DispatchStatus.SEARCHING); // No driver found yet
        }

        return dispatchRepository.save(dispatch);
    }

    public void updateVehicleStatus(String driverId, String status) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByDriverId(driverId);
        if (vehicleOpt.isPresent()) {
            Vehicle vehicle = vehicleOpt.get();
            try {
                vehicle.setStatus(VehicleStatus.valueOf(status));
                vehicleRepository.save(vehicle);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + status);
            }
        } else {
            throw new RuntimeException("Vehicle not found for driver: " + driverId);
        }
    }
}
