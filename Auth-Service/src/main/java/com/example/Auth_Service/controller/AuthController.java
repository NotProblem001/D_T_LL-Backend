package com.example.Auth_Service.controller;

import com.example.Auth_Service.model.User;
import com.example.Auth_Service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j; // Added import for Slf4j

@RestController
@RequestMapping("/auth")
@Slf4j // Added Slf4j annotation
public class AuthController {

    @Autowired
    private AuthService authService; // Changed 'service' to 'authService'

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public String addNewUser(@RequestBody User user) {
        log.info("Register request for user: {}", user.getName());
        return authService.saveUser(user);
    }

    @PostMapping("/token")
    public String getToken(@RequestBody User user) {
        log.info("Token request for user: {}", user.getName());
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getName(), user.getPassword()));
        if (authenticate.isAuthenticated()) {
            return authService.generateToken(user.getName());
        } else {
            throw new RuntimeException("invalid access");
        }
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token) {
        log.info("Validate token request");
        authService.validateToken(token);
        return "Token is valid";
    }

    @Autowired
    private com.example.Auth_Service.service.GoogleAuthService googleAuthService;

    @PostMapping("/google")
    public String loginWithGoogle(@RequestBody java.util.Map<String, String> request) {
        log.info("Google login request received");
        String idToken = request.get("token");
        var payload = googleAuthService.verifyToken(idToken);
        String email = payload.getEmail();
        log.info("Google token verified for email: {}", email);
        String name = (String) payload.get("name"); // Extract name from payload
        return authService.registerOrLoginGoogleUser(email, name);
    }

    @PutMapping("/update")
    public String updateUser(@RequestBody User user) {
        log.info("Update request for user: {}", user.getEmail());
        authService.updateUser(user);
        return "User updated successfully";
    }
}
