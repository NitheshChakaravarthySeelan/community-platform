package com.community.users.authservice.interfaces.controller;

import com.community.users.authservice.application.dto.UserLoggedInDTO;
import com.community.users.authservice.application.service.AuthService;
import com.community.users.authservice.domain.model.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.community.users.authservice.interfaces.dto.LoginRequest;
import com.community.users.authservice.interfaces.dto.RegisterRequest;
import com.community.users.authservice.interfaces.dto.UserValidationResponseDTO; // New import
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.security.core.GrantedAuthority; // New import

import java.util.stream.Collectors; // New import

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Assuming User constructor takes name, email, password. Adjust if different.
            User newUser = new User(request.getUserName(), request.getEmail(), request.getPassword(), null); // Role will be set in AuthService
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

    @GetMapping("/validate")
    public ResponseEntity<UserValidationResponseDTO> validateToken(@RequestHeader("Authorization") String authToken) {
        try {
            if (authToken == null || !authToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            String token = authToken.substring(7);
            User user = authService.validateToken(token);

            // Map User to UserValidationResponseDTO
            UserValidationResponseDTO responseDTO = new UserValidationResponseDTO(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList())
            );

            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
