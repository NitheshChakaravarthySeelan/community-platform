package com.community.users.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication {

    /** Prevents instantiation. */
    public UserServiceApplication() {
        // Private constructor
    }

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args Command line arguments.
     */
    public static void main(final String[] args) { // Made args final
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
