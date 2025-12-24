package com.example.Dispatch_Service.repository;

import com.example.Dispatch_Service.model.Vehicle;
import com.example.Dispatch_Service.model.VehicleStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    Optional<Vehicle> findFirstByStatus(VehicleStatus status);

    Optional<Vehicle> findByDriverId(String driverId);
}
