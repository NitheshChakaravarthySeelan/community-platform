package com.community.users.userservice.interfaces.controller;

import com.community.users.userservice.application.command.CreateUserCommand;
import com.community.users.userservice.application.dto.CreateUserRequestDTO;
import com.community.users.userservice.application.dto.UserResponseDTO;
import com.community.users.userservice.mediator.Mediator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public final class UserController { // Made final to address DesignForExtension

    /** The mediator for dispatching commands. */
    private final Mediator mediator;

    /**
     * Constructs a new UserController.
     *
     * @param theMediator The mediator instance.
     */
    public UserController(final Mediator theMediator) {
        this.mediator = theMediator;
    }

    /**
     * Creates a new user.
     *
     * @param requestDTO The DTO containing user creation details.
     * @return A ResponseEntity containing the created UserResponseDTO.
     */
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody final CreateUserRequestDTO requestDTO) {
        // Map DTO to Command
        CreateUserCommand command =
                new CreateUserCommand(
                        requestDTO.getUserName(), requestDTO.getEmail(), requestDTO.getPassword());
        UserResponseDTO response = mediator.send(command);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
