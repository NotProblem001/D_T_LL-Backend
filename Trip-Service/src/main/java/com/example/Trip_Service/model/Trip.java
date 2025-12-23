package com.example.Trip_Service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "trip")
public class Trip {
    @Id
    private String id;
    private String origin;
    private String destination;
    private Double price;
    private Integer availableSeats;
    private String status;
}
