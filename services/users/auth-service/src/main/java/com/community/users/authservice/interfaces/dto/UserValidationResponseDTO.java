package com.community.users.authservice.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserValidationResponseDTO {
    private Long id;
    private String userName;
    private String email;
    private List<String> roles;
}
