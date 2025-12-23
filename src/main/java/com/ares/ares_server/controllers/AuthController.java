package com.ares.ares_server.controllers;

import com.ares.ares_server.dto.AuthDTO;
import com.ares.ares_server.dto.CredentialsDTO;
import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final AuthService authService;

    /**
     * Register( signup) a new user
     *
     * @param userDTO The new user to create.
     * @return ResponseEntity containing the created user with HTTP status 201 (Created).
     * */
    @Operation(
            summary = "User Signup",
            description = "Registers a new user in the system.",
            tags = {"Auth Operations"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "409",description = "User already exists")
    })
    @PostMapping("/signup")
    public ResponseEntity<AuthDTO> signup(@RequestBody UserDTO userDTO){
        AuthDTO result =  authService.signUp(userDTO);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    /**
     * User login.
     *
     * @param loginCredentials The user login request containing email and password.
     * @return ResponseEntity with user if authenticated, otherwise 401 Unauthorized.
     */
    @Operation(
            summary = "User Login",
            description = "Authenticate user with username and password",
            tags = {"Auth Operations"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "User logged in successfully"),
            @ApiResponse(responseCode = "401",description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthDTO> login(@RequestBody CredentialsDTO loginCredentials){
        String token = authService.login(loginCredentials);
        return new ResponseEntity<>(new AuthDTO(token), HttpStatus.OK);
    }


    /**
     * Refresh token (placeholder for token-based systems).
     */
    @Operation(
            summary = "Refresh Token",
            description = "Refresh authentication token for a user.",
            tags = { "Auth Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthDTO> refresh(@RequestBody CredentialsDTO refreshCredentials) {
        String token = authService.refresh(refreshCredentials);
        return new ResponseEntity<>(new AuthDTO(token), HttpStatus.OK);
    }

    /**
     * Change password (stub).
     */
    @Operation(
            summary = "Change Password",
            description = "Change password for the current user.",
            tags = {"Auth Operations"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{email}/change-password")
    public ResponseEntity<String> changePassword(@PathVariable String email, @RequestBody String newPassword) {
        authService.changePassword(email, newPassword);
        return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
    }
}
