package com.community.users.authservice.interfaces.controller;

import com.community.users.authservice.application.dto.UserLoggedInDTO;
import com.community.users.authservice.application.service.AuthService;
import com.community.users.authservice.domain.model.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.community.users.authservice.interfaces.dto.LoginRequest;
import com.community.users.authservice.interfaces.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User newUser = new User(request.getName(), request.getEmail(), request.getPassword());
            authService.registerUser(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoggedInDTO> login(@Valid @RequestBody LoginRequest request) throws Exception {
        UserLoggedInDTO response = authService.userLogin(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }
}
