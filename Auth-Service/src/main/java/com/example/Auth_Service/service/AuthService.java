package com.example.Auth_Service.service;

import com.example.Auth_Service.model.User;
import com.example.Auth_Service.repository.UserRepository;
import com.example.Auth_Service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public String saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repository.save(user);
        return "user added to the system";
    }

    public String generateToken(String username) {
        return jwtUtil.generateToken(username);
    }

    public void validateToken(String token) {
        jwtUtil.validateToken(token);
    }

    public String registerOrLoginGoogleUser(String email, String name) {
        java.util.Optional<User> existingUser = repository.findByEmail(email);
        if (existingUser.isPresent()) {
            return generateToken(email);
        } else {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setPassword(passwordEncoder.encode("google-oauth-dummy-password")); // Dummy password
            newUser.setRoles(java.util.List.of("ROLE_USER"));
            repository.save(newUser);
            return generateToken(email);
        }
    }

    public User updateUser(User userUpdates) {
        User existingUser = repository.findByEmail(userUpdates.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userUpdates.getName() != null)
            existingUser.setName(userUpdates.getName());
        if (userUpdates.getPhoneNumber() != null)
            existingUser.setPhoneNumber(userUpdates.getPhoneNumber());
        if (userUpdates.getRut() != null)
            existingUser.setRut(userUpdates.getRut());

        return repository.save(existingUser);
    }
}
