package com.community.users.userservice.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserCommand {
    /** The username for the new user. */
    private String userName;

    /** The email address for the new user. */
    private String email;

    /** The raw password for the new user. */
    private String password;
}
