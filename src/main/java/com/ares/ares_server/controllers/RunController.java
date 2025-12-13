package com.ares.ares_server.controllers;

import com.ares.ares_server.dto.RunDTO;
import com.ares.ares_server.service.RunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/runs")
@RequiredArgsConstructor
public class RunController {

    private final RunService runService;

    /**
     * Create a new run in the system.
     */
    @Operation(
            summary = "Create a new Run",
            description = "Create a new run in the system. Validates geometry and updates zones.",
            tags = { "Run Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Run created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided")
    })
    @PostMapping
    public ResponseEntity<RunDTO> createRun(@RequestBody RunDTO runDto) {
        RunDTO createdRun = runService.createRun(runDto);
        return new ResponseEntity<>(createdRun, HttpStatus.CREATED);
    }

    /**
     * Get all runs from the system.
     */
    @Operation(
            summary = "Retrieve all Runs",
            description = "Fetch all runs present in the system.",
            tags = { "Run Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Runs retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<RunDTO>> getAllRuns() {
        List<RunDTO> runs = runService.getAllRuns();
        return new ResponseEntity<>(runs, HttpStatus.OK);
    }

    /**
     * Get a run by its unique ID.
     */
    @Operation(
            summary = "Get Run by ID",
            description = "Retrieve a run using its unique identifier (ID).",
            tags = { "Run Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Run found successfully"),
            @ApiResponse(responseCode = "404", description = "Run not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RunDTO> getRunById(@PathVariable Long id) {
        RunDTO run = runService.getRunById(id);
        return new ResponseEntity<>(run, HttpStatus.OK);
    }

    /**
     * Get runs by a specific owner ID.
     */
    @Operation(
            summary = "Get Runs by Owner ID",
            description = "Retrieve all runs belonging to a specific owner.",
            tags = { "Run Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Runs found successfully")
    })
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<RunDTO>> getRunsByOwner(@PathVariable UUID ownerId) {
        List<RunDTO> runs = runService.getRunsByOwner(ownerId);
        return new ResponseEntity<>(runs, HttpStatus.OK);
    }

    /**
     * Get runs by a specific owner username.
     */
    @Operation(
            summary = "Get Runs by Owner Username",
            description = "Retrieve all runs owned by a specific username.",
            tags = { "Run Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Runs found successfully")
    })
    @GetMapping("/username/{username}")
    public ResponseEntity<List<RunDTO>> getRunsByOwnerUsername(@PathVariable String username) {
        List<RunDTO> runs = runService.getRunsByOwnerUsername(username);
        return new ResponseEntity<>(runs, HttpStatus.OK);
    }

    /**
     * Update an existing run by ID.
     */
    @Operation(
            summary = "Update Run",
            description = "Update an existing run by its unique ID.",
            tags = { "Run Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Run updated successfully"),
            @ApiResponse(responseCode = "404", description = "Run not found for the given ID"),
            @ApiResponse(responseCode = "400", description = "Invalid run data provided")
    })
    @PutMapping("/{id}")
    public ResponseEntity<RunDTO> updateRun(@PathVariable Long id, @RequestBody RunDTO updatedRunDto) {
        RunDTO updatedRun = runService.updateRun(id, updatedRunDto);
        return new ResponseEntity<>(updatedRun, HttpStatus.OK);
    }

    /**
     * Delete a run by its ID.
     */
    @Operation(
            summary = "Delete Run",
            description = "Delete a run from the system by its unique ID.",
            tags = { "Run Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Run deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Run not found for the given ID")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRun(@PathVariable Long id) {
        runService.deleteRun(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
