package com.community.users.authservice.application.service;

import com.community.users.authservice.application.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.community.users.authservice.application.dto.UserLoggedInDTO;
import com.community.users.authservice.domain.model.User;
import com.community.users.authservice.domain.repository.UserRepository;
import com.community.users.authservice.infrastructure.security.JWTService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import com.community.users.authservice.domain.model.Role;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;


    @Autowired
    public AuthService(UserRepository userRepository, JWTService jwtService, PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    public UserLoggedInDTO userLogin(String email, String password) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("Invalid email or password"));

        // The User object itself should implement UserDetails for this to work seamlessly.
        if (passwordEncoder.matches(password, user.getPassword())) {
            // generateToken expects UserDetails. If User implements UserDetails, we can pass it directly.
            String jwtToken = jwtService.generateToken(user);
            UserDTO userDTO = new UserDTO(user.getUsername());

            return UserLoggedInDTO.builder()
                    .jwtToken(jwtToken)
                    .userId(user.getId())
                    .userName(userDTO)
                    .email(user.getEmail())
                    .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                    .build();
        } else {
            throw new Exception("Invalid email or password");
        }
    }

    public User registerUser(User user) throws Exception {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new Exception("User with email " + user.getEmail() + " already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Assuming Role "USER" exists in the database.
        // A better approach would be to have a RoleRepository to fetch the Role object.
        Role userRole = new Role();
        userRole.setName("USER");
        // If Role has an ID that needs to be set, it should be done here.

        user.setRoles(Collections.singletonList(userRole));
        return userRepository.save(user);
    }

    public User validateToken(String token) throws Exception {
        try {
            if (token == null || token.isEmpty()) {
                throw new Exception("Invalid token");
            }

            String username = jwtService.extractUsername(token);
            if (username == null) {
                throw new Exception("Invalid token");
            }

            // The "security guard" flow: load the official record (UserDetails) from the directory (UserDetailsService)
            // and validate the token against that official record.
            User user = (User) userDetailsService.loadUserByUsername(username); // Cast to User
            
            if (jwtService.isTokenValid(token, user)) {
                return user;
            } else {
                throw new Exception("Invalid token");
            }
        } catch (Exception e) {
            throw new Exception("Error validating token");
        }
    }
}
