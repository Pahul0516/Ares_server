package com.ares.ares_server.controllers;

import com.ares.ares_server.dto.ZoneDTO;
import com.ares.ares_server.dto.mappers.ZoneMapper;
import com.ares.ares_server.domain.Zone;
import com.ares.ares_server.repository.ZoneRepository;
import com.ares.ares_server.service.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private ZoneMapper zoneMapper;

    private final ZoneService zoneService;

    @Operation(
            summary = "Create a new Zone",
            description = "Create a new zone in the system. This will save a new zone in the database.",
            tags = {"Zone Operations"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Zone created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided")
    })
    @PostMapping
    public ResponseEntity<ZoneDTO> createZone(@RequestBody ZoneDTO zoneDto) {
        ZoneDTO createdZone = zoneService.createZone(zoneDto);
        return new ResponseEntity<>(createdZone, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Retrieve all Zones",
            description = "Fetch all zones present in the system.",
            tags = {"Zone Operations"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zones retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<ZoneDTO>> getAllZones() {
        return ResponseEntity.ok(zoneService.getAllZones());
    }

    @Operation(
            summary = "Get Zone by ID",
            description = "Retrieve a zone using its unique identifier (ID).",
            tags = {"Zone Operations"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zone found successfully"),
            @ApiResponse(responseCode = "404", description = "Zone not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ZoneDTO> getZoneById(@PathVariable Long id) {
        return ResponseEntity.ok(zoneService.getZoneById(id));
    }

    @Operation(
            summary = "Get Zones by Owner",
            description = "Retrieve all zones owned by a specific owner.",
            tags = {"Zone Operations"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zones found successfully for the owner"),
            @ApiResponse(responseCode = "404", description = "Owner not found")
    })
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<ZoneDTO>> getZonesByOwner(@PathVariable UUID ownerId) {
        return ResponseEntity.ok(zoneService.getZonesByOwner(ownerId));
    }

    @Operation(
            summary = "Update Zone",
            description = "Update an existing zone by its unique ID.",
            tags = {"Zone Operations"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zone updated successfully"),
            @ApiResponse(responseCode = "404", description = "Zone not found for the given ID"),
            @ApiResponse(responseCode = "400", description = "Invalid zone data provided")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ZoneDTO> updateZone(
            @PathVariable Long id,
            @RequestBody ZoneDTO updatedZoneDto
    ) {
        return ResponseEntity.ok(zoneService.updateZone(id, updatedZoneDto));
    }

    @Operation(
            summary = "Delete Zone",
            description = "Delete a zone from the system by its unique ID.",
            tags = {"Zone Operations"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Zone deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Zone not found for the given ID")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        zoneService.deleteZone(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}