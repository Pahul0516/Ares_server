package com.ares.ares_server.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class HelloController {

    // In-memory "database" for demonstration
    private final List<User> users = new ArrayList<>();

    // DTO class for JSON
    public static class User {
        @Schema(description = "User name", example = "Alice")
        public String name;

        public User() {} // default constructor for JSON deserialization

        public User(String name) {
            this.name = name;
        }
    }

    // GET all users
    @Operation(summary = "Get all users", description = "Retrieve a list of all users currently stored")
    @GetMapping
    public List<User> getAllUsers() {
        return users;
    }

    // GET a single user by index
    @Operation(summary = "Get a user by ID", description = "Retrieve a single user by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable int id) {
        if (id < 0 || id >= users.size()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(users.get(id));
    }

    // POST a new user
    @Operation(summary = "Add a new user", description = "Add a new user by providing a JSON object")
    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody(description = "User object to add", required = true,
            content = @Content(schema = @Schema(implementation = User.class))) @org.springframework.web.bind.annotation.RequestBody User user) {
        users.add(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // PUT to update an existing user
    @Operation(summary = "Update a user", description = "Update an existing user by ID with a new name")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable int id,
                                           @RequestBody(description = "Updated user object", required = true,
                                                   content = @Content(schema = @Schema(implementation = User.class))) @org.springframework.web.bind.annotation.RequestBody User user) {
        if (id < 0 || id >= users.size()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        users.set(id, user);
        return ResponseEntity.ok(user);
    }

    // DELETE a user
    @Operation(summary = "Delete a user", description = "Remove a user from the list by its ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {
        if (id < 0 || id >= users.size()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        String removed = users.remove(id).name;
        return ResponseEntity.ok("User deleted: " + removed);
    }

    // Original simple GET examples
    @Operation(summary = "Hello endpoint", description = "Returns a Hello World string")
    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @Operation(summary = "Greet endpoint", description = "Returns a greetings message")
    @GetMapping("/greet")
    public String greetUser() {
        return "Greetings!";
    }
}
