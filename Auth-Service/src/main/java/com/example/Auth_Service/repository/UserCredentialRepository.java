package com.example.Auth_Service.repository;

import com.example.Auth_Service.model.UserCredential;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserCredentialRepository extends MongoRepository<UserCredential, String> {
    Optional<UserCredential> findByName(String username);
}
