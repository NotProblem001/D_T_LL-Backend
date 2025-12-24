package com.example.Dispatch_Service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.geo.Point;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "vehicles")
public class Vehicle {
    @Id
    private String id;
    private String driverId;
    private String plate;
    private String model;
    private String color;
    private VehicleStatus status;
    private Point location; // For geospatial queries
}
