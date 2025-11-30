package com.ares.ares_server.controllers;

import com.ares.ares_server.dto.AuthDTO;
import com.ares.ares_server.dto.CredentialsDTO;
import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.exceptios.InvalidCredentialsException;
import com.ares.ares_server.exceptios.UserAlreadyExistsException;
import com.ares.ares_server.exceptios.UserDoesNotExistsException;
import com.ares.ares_server.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler()) // register exception handler
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void signup_success() throws Exception {
        UserDTO userDTO = new UserDTO("john", "john@test.com", "secret");
        AuthDTO authDTO = new AuthDTO("token123");

        when(authService.signUp(userDTO)).thenReturn(authDTO);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token123"));

        verify(authService).signUp(userDTO);
    }

    @Test
    void signup_conflict_userAlreadyExists() throws Exception {
        UserDTO userDTO = new UserDTO("john", "john@test.com", "secret");

        when(authService.signUp(userDTO))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isConflict())
                .andExpect(content().string("User already exists"));

        verify(authService).signUp(userDTO);
    }

    @Test
    void login_success() throws Exception {
        CredentialsDTO credentials = new CredentialsDTO("john@test.com", "secret");

        when(authService.login(credentials)).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authService).login(credentials);
    }

    @Test
    void login_invalidCredentials() throws Exception {
        CredentialsDTO credentials = new CredentialsDTO("john@test.com", "wrong");

        when(authService.login(credentials))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));

        verify(authService).login(credentials);
    }

    @Test
    void refresh_success() throws Exception {
        CredentialsDTO refreshCredentials = new CredentialsDTO("john@test.com", "refresh-token");

        when(authService.refresh(refreshCredentials)).thenReturn("new-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshCredentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-token"));

        verify(authService).refresh(refreshCredentials);
    }

    @Test
    void refresh_invalidToken() throws Exception {
        CredentialsDTO refreshCredentials = new CredentialsDTO("john@test.com", "bad-token");

        when(authService.refresh(refreshCredentials))
                .thenThrow(new InvalidCredentialsException("Invalid refresh token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshCredentials)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid refresh token"));

        verify(authService).refresh(refreshCredentials);
    }

    @Test
    void changePassword_success() throws Exception {
        String email = "john@test.com";
        String newPassword = "newSecret";

        doNothing().when(authService).changePassword(email, newPassword);

        mockMvc.perform(patch("/api/auth/" + email + "/change-password")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(newPassword))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully"));

        verify(authService).changePassword(email, newPassword);
    }

    @Test
    void changePassword_userNotFound() throws Exception {
        String email = "missing@test.com";
        String newPassword = "newSecret";

        doThrow(new UserDoesNotExistsException("User not found"))
                .when(authService).changePassword(email, newPassword);

        mockMvc.perform(patch("/api/auth/" + email + "/change-password")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(newPassword))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));

        verify(authService).changePassword(email, newPassword);
    }

}