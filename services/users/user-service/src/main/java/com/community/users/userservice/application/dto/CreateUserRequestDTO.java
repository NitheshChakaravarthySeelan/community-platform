package com.community.users.userservice.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDTO {

    /** Minimum length for the username. */
    private static final int USERNAME_MIN_LENGTH = 5;

    /** Maximum length for the username. */
    private static final int USERNAME_MAX_LENGTH = 15;

    /** The username for the new user. */
    @NotBlank(message = "Username cannot be empty")
    @Size(
            min = USERNAME_MIN_LENGTH,
            max = USERNAME_MAX_LENGTH,
            message =
                    "Username must be between "
                            + USERNAME_MIN_LENGTH
                            + "-"
                            + USERNAME_MAX_LENGTH
                            + " characters")
    private String userName;

    /** The email address for the new user. */
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;

    /** Minimum length for the password. */
    private static final int PASSWORD_MIN_LENGTH = 8;

    /** Maximum length for the password. */
    private static final int PASSWORD_MAX_LENGTH = 30;

    /** The password for the new user. */
    @NotBlank(message = "Password cannot be empty")
    @Size(
            min = PASSWORD_MIN_LENGTH,
            max = PASSWORD_MAX_LENGTH,
            message =
                    "Password must be between "
                            + PASSWORD_MIN_LENGTH
                            + " and "
                            + PASSWORD_MAX_LENGTH
                            + " characters")
    private String password;
}
