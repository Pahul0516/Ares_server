package com.ares.ares_server.Controllers;

import com.ares.ares_server.DTOs.ZoneDTO;
import com.ares.ares_server.DTOs.Mappers.ZoneMapper;
import com.ares.ares_server.Domain.Zone;
import com.ares.ares_server.Repository.ZoneRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private ZoneMapper zoneMapper;

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
    public ResponseEntity<ZoneDTO> createZone(@RequestBody ZoneDTO zoneDto) {
        Zone zone = zoneMapper.fromDto(zoneDto);
        Zone savedZone = zoneRepository.save(zone);
        return new ResponseEntity<>(zoneMapper.toDto(savedZone), HttpStatus.CREATED);
    }

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
    public ResponseEntity<List<ZoneDTO>> getAllZones() {
        List<Zone> zones = zoneRepository.findAll();
        List<ZoneDTO> zoneDtos = zones.stream()
                .map(zoneMapper::toDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(zoneDtos, HttpStatus.OK);
    }

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
    public ResponseEntity<ZoneDTO> getZoneById(@PathVariable Long id) {
        Optional<Zone> zone = zoneRepository.findById(id);
        return zone.map(value -> new ResponseEntity<>(zoneMapper.toDto(value), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

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
    public ResponseEntity<List<ZoneDTO>> getZonesByOwner(@PathVariable UUID ownerId) {
        List<Zone> zones = zoneRepository.findByOwnerId(ownerId);
        List<ZoneDTO> zoneDtos = zones.stream()
                .map(zoneMapper::toDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(zoneDtos, HttpStatus.OK);
    }

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
    public ResponseEntity<ZoneDTO> updateZone(@PathVariable Long id, @RequestBody ZoneDTO updatedZoneDto) {
        if (!zoneRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Zone updatedZone = zoneMapper.fromDto(updatedZoneDto);
        updatedZone.setId(id); // Ensure the ID is set for update
        Zone savedZone = zoneRepository.save(updatedZone);
        return new ResponseEntity<>(zoneMapper.toDto(savedZone), HttpStatus.OK);
    }

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