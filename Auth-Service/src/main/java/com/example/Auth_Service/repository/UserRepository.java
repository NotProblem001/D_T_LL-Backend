package com.example.Auth_Service.repository;

import com.example.Auth_Service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);
}
