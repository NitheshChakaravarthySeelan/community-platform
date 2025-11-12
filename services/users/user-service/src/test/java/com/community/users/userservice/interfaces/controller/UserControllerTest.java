package com.community.users.userservice.interfaces.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.community.users.userservice.application.dto.CreateUserRequestDTO;
import com.community.users.userservice.application.dto.UserResponseDTO;
import com.community.users.userservice.config.SecurityConfig;
import com.community.users.userservice.mediator.Mediator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private Mediator mediator;

    @Autowired private ObjectMapper objectMapper;

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        CreateUserRequestDTO requestDTO =
                CreateUserRequestDTO.builder()
                        .userName("testuser")
                        .email("test@example.com")
                        .password("password123")
                        .build();

        UserResponseDTO expectedResponse =
                UserResponseDTO.builder()
                        .userId(1L)
                        .userName("testuser")
                        .email("test@example.com")
                        .build();

        when(mediator.send(any())).thenReturn(expectedResponse);

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.userName").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void createUser_shouldReturnBadRequest_whenInvalidInput() throws Exception {
        CreateUserRequestDTO requestDTO =
                CreateUserRequestDTO.builder()
                        .userName("usr") // Too short
                        .email("invalid-email") // Invalid email
                        .password("") // Empty password
                        .build();

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }
}
