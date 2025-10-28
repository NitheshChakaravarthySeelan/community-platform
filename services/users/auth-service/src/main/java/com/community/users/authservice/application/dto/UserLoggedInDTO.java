package com.community.users.authservice.application.dto;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserLoggedInDTO {
    private String jwtToken;
    private Long userId;
    private UserDTO userName;
    private String email;
    private List<String> roles;

    @Override
    public String toString() {
        return "UserLoggedInDTO{" +
                "jwtToken='" + jwtToken + '\'' +
                ", userId=" + userId +
                ", userName=" + userName +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                '}';
    }
}
