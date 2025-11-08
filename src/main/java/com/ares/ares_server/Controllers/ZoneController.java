package com.ares.ares_server.Controllers;

import com.ares.ares_server.Domain.Zone;
import com.ares.ares_server.Repository.ZoneRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/zones")
public class ZoneController {

    @Autowired
    private ZoneRepository zoneRepository;

    /**
     * Create a new zone in the system.
     *
     * @param zone The zone object to be created.
     * @return ResponseEntity containing the created zone with HTTP status 201 (Created).
     */
    @Operation(
            summary = "Create a new Zone",
            description = "Create a new zone in the system. This will save a new zone in the database.",
            tags = { "Zone Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Zone created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided")
    })
    @PostMapping
    public ResponseEntity<Zone> createZone(@RequestBody Zone zone) {
        Zone savedZone = zoneRepository.save(zone);
        return new ResponseEntity<>(savedZone, HttpStatus.CREATED);
    }

    /**
     * Get all zones from the system.
     *
     * @return ResponseEntity containing the list of all zones.
     */
    @Operation(
            summary = "Retrieve all Zones",
            description = "Fetch all zones present in the system.",
            tags = { "Zone Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zones retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<Zone>> getAllZones() {
        List<Zone> zones = zoneRepository.findAll();
        return new ResponseEntity<>(zones, HttpStatus.OK);
    }

    /**
     * Get a zone by its unique ID.
     *
     * @param id The ID of the zone to retrieve.
     * @return ResponseEntity containing the zone if found, otherwise 404 Not Found.
     */
    @Operation(
            summary = "Get Zone by ID",
            description = "Retrieve a zone using its unique identifier (ID).",
            tags = { "Zone Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zone found successfully"),
            @ApiResponse(responseCode = "404", description = "Zone not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Zone> getZoneById(@PathVariable Long id) {
        Optional<Zone> zone = zoneRepository.findById(id);
        if (zone.isPresent()) {
            return new ResponseEntity<>(zone.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get zones by a specific owner.
     *
     * @param ownerId The owner ID to filter zones.
     * @return List of zones belonging to the specific owner.
     */
    @Operation(
            summary = "Get Zones by Owner",
            description = "Retrieve all zones owned by a specific owner.",
            tags = { "Zone Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zones found successfully for the owner"),
            @ApiResponse(responseCode = "404", description = "Owner not found")
    })
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Zone>> getZonesByOwner(@PathVariable UUID ownerId) {
        List<Zone> zones = zoneRepository.findByOwnerId(ownerId);
        return new ResponseEntity<>(zones, HttpStatus.OK);
    }

    /**
     * Update an existing zone by ID.
     *
     * @param id The ID of the zone to be updated.
     * @param updatedZone The new zone data for update.
     * @return ResponseEntity containing the updated zone, or 404 if zone not found.
     */
    @Operation(
            summary = "Update Zone",
            description = "Update an existing zone by its unique ID.",
            tags = { "Zone Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zone updated successfully"),
            @ApiResponse(responseCode = "404", description = "Zone not found for the given ID"),
            @ApiResponse(responseCode = "400", description = "Invalid zone data provided")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Zone> updateZone(@PathVariable Long id, @RequestBody Zone updatedZone) {
        if (!zoneRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        updatedZone.setId(id); // Ensure the ID is set for update
        Zone savedZone = zoneRepository.save(updatedZone);
        return new ResponseEntity<>(savedZone, HttpStatus.OK);
    }

    /**
     * Delete a zone by its ID.
     *
     * @param id The ID of the zone to be deleted.
     * @return ResponseEntity with 204 status code for successful deletion.
     */
    @Operation(
            summary = "Delete Zone",
            description = "Delete a zone from the system by its unique ID.",
            tags = { "Zone Operations" }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Zone deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Zone not found for the given ID")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        if (!zoneRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        zoneRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
