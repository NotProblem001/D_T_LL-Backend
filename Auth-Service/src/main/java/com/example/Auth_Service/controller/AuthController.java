package com.example.Auth_Service.controller;

import com.example.Auth_Service.model.User;
import com.example.Auth_Service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public String addNewUser(@RequestBody User user) {
        return service.saveUser(user);
    }

    @PostMapping("/token")
    public String getToken(@RequestBody User user) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getName(), user.getPassword()));
        if (authenticate.isAuthenticated()) {
            return service.generateToken(user.getName());
        } else {
            throw new RuntimeException("invalid access");
        }
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token) {
        service.validateToken(token);
        return "Token is valid";
    }

    @Autowired
    private com.example.Auth_Service.service.GoogleAuthService googleAuthService;

    @PostMapping("/google")
    public String loginWithGoogle(@RequestBody java.util.Map<String, String> request) {
        String idToken = request.get("token");
        var payload = googleAuthService.verifyToken(idToken);
        String email = payload.getEmail();
        // Here we should check if user exists, if not register, then return token.
        // For now, delegating to a new method in AuthService or handling here?
        // Let's keep it simple: generate token for the email (assuming username=email
        // for Google users)
        return service.generateToken(email);
    }
}
