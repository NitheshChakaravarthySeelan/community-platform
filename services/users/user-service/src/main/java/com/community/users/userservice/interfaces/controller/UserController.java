package com.community.users.userservice.interfaces.controller;

import com.community.users.userservice.application.command.CreateUserCommand;
import com.community.users.userservice.application.command.GetUserByIdQuery;
import com.community.users.userservice.application.dto.CreateUserRequestDTO;
import com.community.users.userservice.application.dto.UserResponseDTO;
import com.community.users.userservice.mediator.Mediator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param userId The unique identifier of the user.
     * @return A ResponseEntity containing the UserResponseDTO if found, or a 404 Not Found status.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable final Long userId) {
        GetUserByIdQuery query = new GetUserByIdQuery(userId);
        UserResponseDTO response = mediator.send(query);
        // Assuming mediator.send will throw an exception if user not found,
        // which will be handled by a global exception handler.
        return ResponseEntity.ok(response);
    }
}
