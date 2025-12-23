package com.ares.ares_server.service;

import com.ares.ares_server.domain.Run;
import com.ares.ares_server.dto.RunDTO;
import com.ares.ares_server.dto.mappers.RunMapper;
import com.ares.ares_server.exceptions.RunDoesNotExistException;
import com.ares.ares_server.repository.RunRepository;
import com.ares.ares_server.utils.GeometryProjectionUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RunService {

    private final RunRepository runRepository;
    private final RunMapper runMapper;
    private final ZoneService zoneService;

    /**
     * Create a new run with geometry validation and zone update.
     */
    @Transactional
    public RunDTO createRun(RunDTO runDto) {
        Run run = runMapper.fromDto(runDto);
        Geometry geom = run.getPolygon();

        if (!(geom instanceof Polygon polygon)) {
            throw new IllegalArgumentException("Provided geometry must be a Polygon.");
        }

        // Validate polygon closure
        Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
        Coordinate first = coords[0];
        Coordinate last = coords[coords.length - 1];

        Point p1 = geom.getFactory().createPoint(first);
        Point p2 = geom.getFactory().createPoint(last);
        Geometry buffer = GeometryProjectionUtil.bufferInMeters(p1, 10);

        // If last point is too far from first → invalid
        if (!buffer.contains(p2)) {
            throw new IllegalArgumentException("Polygon must be closed: last coordinate too far from first.");
        }

        // Force closure if first != last
        if (!first.equals2D(last)) {
            coords[coords.length - 1] = first;
            LinearRing shell = geom.getFactory().createLinearRing(coords);
            Polygon closedPolygon = geom.getFactory().createPolygon(shell, null);
            run.setPolygon(closedPolygon);
        }

        run.setCreatedAt(OffsetDateTime.now());

        // Save initial run
        Run savedRun = runRepository.save(run);

        // Update zones and save again (areaGained)
        zoneService.updateZonesForRun(savedRun);
        savedRun = runRepository.save(savedRun);

        return runMapper.toDto(savedRun);
    }

    /**
     * Retrieve all runs.
     */
    public List<RunDTO> getAllRuns() {
        return runRepository.findAll()
                .stream()
                .map(runMapper::toDto)
                .toList();
    }

    /**
     * Retrieve a run by ID.
     */
    public RunDTO getRunById(Long id) {
        Run run = runRepository.findById(id)
                .orElseThrow(() -> new RunDoesNotExistException(
                        "Run with id " + id + " does not exist!"
                ));
        return runMapper.toDto(run);
    }

    /**
     * Retrieve runs by owner ID.
     */
    public List<RunDTO> getRunsByOwner(UUID ownerId) {
        List<Run> runs = runRepository.findByOwnerId(ownerId);

        if (runs.isEmpty()) {
            throw new RunDoesNotExistException("No runs found for owner with id " + ownerId);
        }

        return runs.stream()
                .map(runMapper::toDto)
                .toList();
    }

    /**
     * Retrieve runs by owner username.
     */
    public List<RunDTO> getRunsByOwnerUsername(String username) {
        List<Run> runs = runRepository.findByOwnerUsername(username);
        if (runs.isEmpty()) {
            throw new RunDoesNotExistException("No runs found for owner with username " + username);
        }

        return runs.stream()
                .map(runMapper::toDto)
                .toList();
    }

    /**
     * Update a run.
     */
    @Transactional
    public RunDTO updateRun(Long id, RunDTO updatedRunDto) {
        if (!runRepository.existsById(id)) {
            throw new RunDoesNotExistException("Run with id " + id + " does not exist!");
        }

        Run runToUpdate = runMapper.fromDto(updatedRunDto);
        runToUpdate.setId(id);

        Run savedRun = runRepository.save(runToUpdate);
        return runMapper.toDto(savedRun);
    }

    /**
     * Delete a run.
     */
    @Transactional
    public void deleteRun(Long id) {
        if (!runRepository.existsById(id)) {
            throw new RunDoesNotExistException("Run with id " + id + " does not exist!");
        }
        runRepository.deleteById(id);
    }
}
