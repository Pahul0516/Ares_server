package com.ares.ares_server.controllers;

import com.ares.ares_server.dto.RunDTO;
import com.ares.ares_server.domain.Run;
import com.ares.ares_server.dto.mappers.RunMapper;
import com.ares.ares_server.repository.RunRepository;
import com.ares.ares_server.service.ZoneService;
import com.ares.ares_server.utils.GeometryProjectionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.locationtech.jts.geom.*;
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

    @Autowired
    private RunMapper runMapper;

    @Autowired
    private ZoneService zoneService;

    /**
     * Create a new run in the system.
     * Also updates adjacent zones to include/exclude the polygon depending on the owner
     *
     * @param runDto The run object to be created.
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
    public ResponseEntity<RunDTO> createRun(@RequestBody RunDTO runDto) {
        Run run = runMapper.fromDto(runDto);
        Geometry geom = run.getPolygon();

        Polygon polygon = null;
        if (geom instanceof Polygon p) {
            polygon = p;
        }

        Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
        Coordinate first = coords[0];
        Coordinate last = coords[coords.length - 1];

        Point p1 = geom.getFactory().createPoint(first);
        Point p2 = geom.getFactory().createPoint(last);
        Geometry buffer = GeometryProjectionUtil.bufferInMeters(p1, 10);

        if (!buffer.contains(p2)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if (!first.equals2D(last)) {
            coords[coords.length - 1] = first;
            LinearRing shell = geom.getFactory().createLinearRing(coords);
            Polygon closedPolygon = geom.getFactory().createPolygon(shell, null);
            run.setPolygon(closedPolygon);
        }

        Run savedRun = runRepository.save(run);
        zoneService.updateZonesForRun(savedRun);
        savedRun = runRepository.save(savedRun); // update with areaGained

        return new ResponseEntity<>(runMapper.toDto(savedRun), HttpStatus.CREATED);
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
    public ResponseEntity<List<RunDTO>> getAllRuns() {
        List<Run> runs = runRepository.findAll();
        List<RunDTO> runDtos = runs.stream()
                .map(runMapper::toDto)
                .toList();
        return new ResponseEntity<>(runDtos, HttpStatus.OK);
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
    public ResponseEntity<RunDTO> getRunById(@PathVariable Long id) {
        Optional<Run> run = runRepository.findById(id);
        if (run.isPresent()) {
            return new ResponseEntity<>(runMapper.toDto(run.get()), HttpStatus.OK);
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
    public ResponseEntity<List<RunDTO>> getRunsByOwner(@PathVariable UUID ownerId) {
        List<Run> runs = runRepository.findByOwnerId(ownerId);
        List<RunDTO> runDtos = runs.stream()
                .map(runMapper::toDto)
                .toList();
        return new ResponseEntity<>(runDtos, HttpStatus.OK);
    }

    /**
     * Get runs by a specific owner username.
     *
     * @param username The owner username to filter runs.
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
    @GetMapping("/username/{username}")
    public ResponseEntity<List<RunDTO>> getRunsByOwner(@PathVariable String username) {
        List<Run> runs = runRepository.findByOwnerUsername(username);
        List<RunDTO> runDtos = runs.stream()
                .map(runMapper::toDto)
                .toList();
        return new ResponseEntity<>(runDtos, HttpStatus.OK);
    }

    /**
     * Update an existing run by ID.
     *
     * @param id The ID of the run to be updated.
     * @param updatedRunDto The new run data for update.
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
    public ResponseEntity<RunDTO> updateRun(@PathVariable Long id, @RequestBody RunDTO updatedRunDto) {
        if (!runRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Run runToUpdate = runMapper.fromDto(updatedRunDto);
        runToUpdate.setId(id);
        Run savedRun = runRepository.save(runToUpdate);
        return new ResponseEntity<>(runMapper.toDto(savedRun), HttpStatus.OK);
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


