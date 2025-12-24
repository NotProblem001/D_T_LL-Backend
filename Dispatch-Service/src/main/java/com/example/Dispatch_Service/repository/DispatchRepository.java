package com.example.Dispatch_Service.repository;

import com.example.Dispatch_Service.model.Dispatch;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DispatchRepository extends MongoRepository<Dispatch, String> {
}
