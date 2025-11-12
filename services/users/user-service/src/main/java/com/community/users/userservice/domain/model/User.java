package com.community.users.userservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users") // Renamed to avoid conflict with SQL keyword
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /** The unique identifier for the user. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The username of the user. Must be unique and not null. */
    @Column(unique = true, nullable = false)
    private String userName;

    /** The email address of the user. Must be unique and not null. */
    @Column(unique = true, nullable = false)
    private String email;

    /** The hashed password of the user. Must not be null. */
    @Column(nullable = false)
    private String password; // Storing hashed password
}
