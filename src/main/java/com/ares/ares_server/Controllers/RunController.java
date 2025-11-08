package com.ares.ares_server.Controllers;

import com.ares.ares_server.Domain.Run;
import com.ares.ares_server.Repository.RunRepository;
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
@RequestMapping("/api/runs")
public class RunController {

    @Autowired
    private RunRepository runRepository;

    /**
     * Create a new run in the system.
     *
     * @param run The run object to be created.
     * @return ResponseEntity containing the created run with HTTP status 201 (Created).
     */
    @Operation(
            summary = "Create a new Run",
            description = "Create a new run in the system. This will save a new run in the database.",
            tags = { "Run Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Run created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided")
    })
    @PostMapping
    public ResponseEntity<Run> createRun(@RequestBody Run run) {
        Run savedRun = runRepository.save(run);
        return new ResponseEntity<>(savedRun, HttpStatus.CREATED);
    }

    /**
     * Get all runs from the system.
     *
     * @return ResponseEntity containing the list of all runs.
     */
    @Operation(
            summary = "Retrieve all Runs",
            description = "Fetch all runs present in the system.",
            tags = { "Run Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Runs retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<Run>> getAllRuns() {
        List<Run> runs = runRepository.findAll();
        return new ResponseEntity<>(runs, HttpStatus.OK);
    }

    /**
     * Get a run by its unique ID.
     *
     * @param id The ID of the run to retrieve.
     * @return ResponseEntity containing the run if found, otherwise 404 Not Found.
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
    public ResponseEntity<Run> getRunById(@PathVariable Long id) {
        Optional<Run> run = runRepository.findById(id);
        if (run.isPresent()) {
            return new ResponseEntity<>(run.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get runs by a specific owner.
     *
     * @param ownerId The owner ID to filter runs.
     * @return List of runs belonging to the specific owner.
     */
    @Operation(
            summary = "Get Runs by Owner",
            description = "Retrieve all runs owned by a specific owner.",
            tags = { "Run Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Runs found successfully for the owner"),
            @ApiResponse(responseCode = "404", description = "Owner not found")
    })
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Run>> getRunsByOwner(@PathVariable UUID ownerId) {
        List<Run> runs = runRepository.findByOwnerId(ownerId);
        return new ResponseEntity<>(runs, HttpStatus.OK);
    }

    /**
     * Update an existing run by ID.
     *
     * @param id The ID of the run to be updated.
     * @param updatedRun The new run data for update.
     * @return ResponseEntity containing the updated run, or 404 if run not found.
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
    public ResponseEntity<Run> updateRun(@PathVariable Long id, @RequestBody Run updatedRun) {
        if (!runRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        updatedRun.setId(id); // Ensure the ID is set for update
        Run savedRun = runRepository.save(updatedRun);
        return new ResponseEntity<>(savedRun, HttpStatus.OK);
    }

    /**
     * Delete a run by its ID.
     *
     * @param id The ID of the run to be deleted.
     * @return ResponseEntity with 204 status code for successful deletion.
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
        if (!runRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        runRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
