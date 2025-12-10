package com.community.users.userservice.application.command;

import com.community.users.userservice.application.dto.UserResponseDTO;
import com.community.users.userservice.domain.model.User;
import com.community.users.userservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetUserByIdQueryHandler {

    private final UserRepository userRepository;

    public UserResponseDTO handle(GetUserByIdQuery query) {
        Optional<User> userOptional = userRepository.findById(query.getUserId());

        if (userOptional.isEmpty()) {
            // In a real application, you might throw a specific NotFoundException
            // For now, let's return null or an empty DTO, or throw a generic RuntimeException
            // For demonstration, we'll return null, but a proper error handling mechanism
            // should be in place (e.g., throwing custom exceptions and handling them with @ControllerAdvice).
            throw new RuntimeException("User not found with ID: " + query.getUserId());
        }

        User user = userOptional.get();
        return UserResponseDTO.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .build();
    }
}
