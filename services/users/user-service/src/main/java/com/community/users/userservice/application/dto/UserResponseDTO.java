package com.community.users.userservice.application.dto;

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
public class UserResponseDTO {
    /** The unique identifier of the user. */
    private Long userId;

    /** The username of the user. */
    private String userName;

    /** The email address of the user. */
    private String email;
}
