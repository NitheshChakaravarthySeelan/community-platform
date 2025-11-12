package com.community.users.userservice.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.community.users.userservice.application.command.CreateUserCommand;
import com.community.users.userservice.application.dto.UserResponseDTO;
import com.community.users.userservice.domain.model.User;
import com.community.users.userservice.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CreateUserCommandHandlerTest {

    @Mock private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private CreateUserCommandHandler handler;

    private CreateUserCommand command;
    private User user;
    private User savedUser;

    @BeforeEach
    void setUp() {
        command = new CreateUserCommand("testuser", "test@example.com", "rawpassword");

        user =
                User.builder()
                        .userName("testuser")
                        .email("test@example.com")
                        .password("hashedpassword") // This will be the encoded password
                        .build();

        savedUser =
                User.builder()
                        .id(1L)
                        .userName("testuser")
                        .email("test@example.com")
                        .password("hashedpassword")
                        .build();
    }

    @Test
    void handle_shouldCreateUserAndReturnResponse() {
        // Mocking behavior
        when(passwordEncoder.encode(command.getPassword())).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Execute the handler
        UserResponseDTO response = handler.handle(command);

        // Assertions
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUserName()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }
}
