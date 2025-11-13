package com.ares.ares_server.Controllers;

import com.ares.ares_server.Domain.User;
import com.ares.ares_server.Repository.UserRepositroy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepositroy userRepository;

    public UserController(UserRepositroy userRepository) {
        this.userRepository = userRepository;
    }


    /**
     * Register( signup) a new user
     *
     * @param user The new user to create.
     * @return ResponseEntity containing the created user with HTTP status 201 (Created).
     * */
    @Operation(
            summary = "User Signup",
            description = "Registers a new user in the system.",
            tags = {"User Operations"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400",description = "Invalid user data")
    })
    @PostMapping("/signup")
    public ResponseEntity<User> signup(@RequestBody User user){
        if(userRepository.existsByEmail(user.getEmail())){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        User savedUser = userRepository.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }


    /**
     * User login.
     *
     * @param userLogin The user login request contaiining email and password.
     * @return ResponseEntity with user if authenticated, otherwise 401 Unauthorized.
     */
    @Operation(
            summary = "User Login",
            description = "Authenticate user with username and password",
            tags = {"User Operations"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "User logged in successfully"),
            @ApiResponse(responseCode = "401",description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User userLogin){
        Optional<User> user = userRepository.findByUsername(userLogin.getUsername());
        if(user.isPresent() && user.get().getEncryptedPassword().equals(userLogin.getEncryptedPassword())){
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }


    /**
     * TODO
     * Logout endoint( placeholder,token-based logout can be added later)
     */
    @Operation(
            summary = "User Logout",
            description = "Logs out a user (token invalidation or session end).",
            tags = {"User Operations"}
    )
    @ApiResponse(responseCode = "200",description = "User logged out successfully")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(){
        return new ResponseEntity<>("User logged out",HttpStatus.OK);
    }



    /**
     * TODO
     * Refresh token (placeholder for token-based systems).
     */
    @Operation(
            summary = "Refresh Token",
            description = "Refresh authentication token for a user.",
            tags = { "User Operations" }
    )
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @PostMapping("/refresh")
    public ResponseEntity<String> refresh() {
        return new ResponseEntity<>("Token refreshed successfully", HttpStatus.OK);
    }

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
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * Retrieve a single user by ID.
     */
    @Operation(
            summary = "Get User by ID",
            description = "Retrieve user details using their unique ID.",
            tags = { "User Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    /**
     * Update user information.
     */
    @Operation(
            summary = "Update User",
            description = "Update user email or username by ID.",
            tags = { "User Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User updatedUser) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        User user = userOpt.get();
        user.setEmail(updatedUser.getEmail());
        user.setUsername(updatedUser.getUsername());
        User savedUser = userRepository.save(user);

        return new ResponseEntity<>(savedUser, HttpStatus.OK);
    }


    /**
     * Delete user by ID.
     */
    @Operation(
            summary = "Delete User",
            description = "Delete a user account by ID.",
            tags = { "User Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (!userRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }




    /**
     * TODO
     * Forgot password endpoint (stub).
     */
    @Operation(
            summary = "Forgot Password",
            description = "Initiate password reset process for a user.",
            tags = { "User Operations" }
    )
    @ApiResponse(responseCode = "200", description = "Password reset link sent")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody String email) {
        email = email.replace("\"", ""); // remove surrounding quotes
        return new ResponseEntity<>("Password reset link sent to " + email, HttpStatus.OK);
    }

    /**
     * Reset password (stub).
     */
    @Operation(
            summary = "Reset Password",
            description = "Reset user password using a token.",
            tags = { "User Operations" }
    )
    @ApiResponse(responseCode = "200", description = "Password reset successfully")
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody String newPassword) {
        return new ResponseEntity<>("Password reset successfully", HttpStatus.OK);
    }

    /**
     * Change password (stub).
     */
    @Operation(
            summary = "Change Password",
            description = "Change password for the current user.",
            tags = { "User Operations" }
    )
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @PatchMapping("/{id}/change-password")
    public ResponseEntity<String> changePassword(@PathVariable UUID id, @RequestBody String newPassword) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User user = userOpt.get();
        user.setEncryptedPassword(newPassword);
        userRepository.save(user);
        return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
    }




}
