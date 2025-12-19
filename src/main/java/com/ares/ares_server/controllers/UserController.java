package com.ares.ares_server.controllers;

import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.dto.UserStatsDTO;
import com.ares.ares_server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieve all users.
     */
    @Operation(
            summary = "Get All Users",
            description = "Retrieve all registered users from the system.",
            tags = { "User Operations" }
    )
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * Retrieve a single user by Email.
     */
    @Operation(
            summary = "Get User by Email",
            description = "Retrieve user details using their unique Email.",
            tags = { "User Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        UserDTO user =  userService.getUserByEmail(email);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Update user information.
     */
    @Operation(
            summary = "Update User",
            description = "Update user by Email.",
            tags = { "User Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{email}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String email, @RequestBody UserDTO updatedUserDto) {
        UserDTO updatedUser = userService.updateUser(email, updatedUserDto);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    /**
     * Delete user by Email.
     */
    @Operation(
            summary = "Delete User",
            description = "Delete a user account by Email.",
            tags = { "User Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
       userService.deleteUser(email);
       return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Retrieve stats for a user by Email.
     */
    @Operation(
            summary = "Get User Stats by Email",
            description = "Retrieve user stats using their unique Email.",
            tags = { "User Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User stats found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{email}/stats")
    public ResponseEntity<UserStatsDTO> getUserStats(@PathVariable String email) {
        UserStatsDTO userStatsDTO = userService.getUserStats(email);
        return new ResponseEntity<>(userStatsDTO, HttpStatus.OK);
    }
}