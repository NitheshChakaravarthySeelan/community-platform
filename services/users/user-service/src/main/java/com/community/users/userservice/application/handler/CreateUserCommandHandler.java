package com.community.users.userservice.application.handler;

import com.community.users.userservice.application.command.CreateUserCommand;
import com.community.users.userservice.application.dto.UserResponseDTO;
import com.community.users.userservice.domain.model.User;
import com.community.users.userservice.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CreateUserCommandHandler {

    /** Repository for User entities. */
    private final UserRepository userRepository;

    /** Encoder for hashing passwords. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a new CreateUserCommandHandler.
     *
     * @param theUserRepository The user repository.
     * @param thePasswordEncoder Password encoder.
     */
    public CreateUserCommandHandler(
            final UserRepository theUserRepository, final PasswordEncoder thePasswordEncoder) {
        this.userRepository = theUserRepository;
        this.passwordEncoder = thePasswordEncoder;
    }

    /**
     * Handles the CreateUserCommand to create a new user.
     *
     * @param command The command containing user creation details.
     * @return A UserResponseDTO with details of the created user.
     */
    public UserResponseDTO handle(final CreateUserCommand command) {
        User user =
                User.builder()
                        .userName(command.getUserName())
                        .email(command.getEmail())
                        .password(passwordEncoder.encode(command.getPassword()))
                        .build();

        User savedUser = userRepository.save(user);

        return UserResponseDTO.builder()
                .userId(savedUser.getId())
                .userName(savedUser.getUserName())
                .email(savedUser.getEmail())
                .build();
    }
}
