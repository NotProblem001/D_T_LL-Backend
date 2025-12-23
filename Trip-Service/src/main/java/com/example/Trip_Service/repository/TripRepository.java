package com.example.Trip_Service.repository;

import com.example.Trip_Service.model.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TripRepository extends MongoRepository<Trip, String> {
}
