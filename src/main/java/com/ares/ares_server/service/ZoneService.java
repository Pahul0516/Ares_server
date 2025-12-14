package com.ares.ares_server.service;

import com.ares.ares_server.domain.Run;
import com.ares.ares_server.domain.User;
import com.ares.ares_server.domain.Zone;
import com.ares.ares_server.dto.ZoneDTO;
import com.ares.ares_server.dto.mappers.ZoneMapper;
import com.ares.ares_server.exceptios.ZoneDoesNotExistException;
import com.ares.ares_server.repository.ZoneRepository;
import com.ares.ares_server.utils.GeometryProjectionUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private static final double AREA_EPSILON = 30;
    private final ZoneMapper zoneMapper;

    @Transactional
    public ZoneDTO createZone(ZoneDTO zoneDto) {
        Zone zone = zoneMapper.fromDto(zoneDto);
        Zone savedZone = zoneRepository.save(zone);
        return zoneMapper.toDto(savedZone);
    }

    /**
     * Retrieve all zones.
     */
    public List<ZoneDTO> getAllZones() {
        return zoneRepository.findAll()
                .stream()
                .map(zoneMapper::toDto)
                .toList();
    }

    /**
     * Retrieve a zone by ID.
     */
    public ZoneDTO getZoneById(Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() ->
                        new ZoneDoesNotExistException("Zone with id " + id + " does not exist!")
                );
        return zoneMapper.toDto(zone);
    }

    /**
     * Retrieve zones by owner ID.
     */
    public List<ZoneDTO> getZonesByOwner(UUID ownerId) {
        List<Zone> zones = zoneRepository.findByOwnerId(ownerId);

        if (zones.isEmpty()) {
            throw new ZoneDoesNotExistException("No zones found for owner with id " + ownerId);
        }

        return zones.stream()
                .map(zoneMapper::toDto)
                .toList();
    }

    /**
     * Update an existing zone.
     */
    @Transactional
    public ZoneDTO updateZone(Long id, ZoneDTO updatedZoneDto) {
        if (!zoneRepository.existsById(id)) {
            throw new ZoneDoesNotExistException("Zone with id " + id + " does not exist!");
        }

        Zone zone = zoneMapper.fromDto(updatedZoneDto);
        zone.setId(id);

        Zone savedZone = zoneRepository.save(zone);
        return zoneMapper.toDto(savedZone);
    }

    /**
     * Delete a zone.
     */
    @Transactional
    public void deleteZone(Long id) {
        if (!zoneRepository.existsById(id)) {
            throw new ZoneDoesNotExistException("Zone with id " + id + " does not exist!");
        }
        zoneRepository.deleteById(id);
    }


    @Transactional
    public void updateZonesForRun(Run run) {
        // original run geometry (fix topology)
        Geometry runGeom = fixGeometry(run.getPolygon());

        // a slightly larger search buffer so we pick touching zones reliably
        Geometry searchGeom = GeometryProjectionUtil.bufferInMeters(runGeom, 10);

        List<Zone> touchingZones = zoneRepository.findZonesIntersecting(searchGeom);

        // split touching zones into my zones and other users' zones
        List<Zone> myZones = new ArrayList<>();
        List<Zone> otherZones = new ArrayList<>();
        for (Zone z : touchingZones) {
            if (z.getOwner().getId().equals(run.getOwner().getId())) {
                myZones.add(z);
            } else {
                otherZones.add(z);
            }
        }

        Geometry enemyUnion = null;
        for (Zone z : otherZones) {
            Geometry zg = fixGeometry(z.getPolygon());
            enemyUnion = (enemyUnion == null) ? zg : fixGeometry(enemyUnion.union(zg));
        }

        // --- CORE BUG FIX: Subtraction must use the original runGeom, but the result must be filtered. ---
        for (Zone z : otherZones) {
            subtractRunFromZone(z, runGeom);
        }

        // Merge the net conquered area (conquered) and user's touching zones (myZones)
        mergeConqueredIntoUserZones(myZones, runGeom, run.getOwner(), run);
    }

    private void subtractRunFromZone(Zone zone, Geometry runGeom) {
        Geometry zoneGeom = fixGeometry(zone.getPolygon());
        Geometry result = zoneGeom.difference(runGeom);

        if (result.isEmpty()) {
            zoneRepository.delete(zone);
            return;
        }

        // FIX: Extract valid polygons (filtering slivers) immediately after subtraction.
        // If the entire zone is reduced to slivers, it is deleted.
        List<Polygon> remainingPolys = extractValidPolygons(result);

        if (remainingPolys.isEmpty()) {
            zoneRepository.delete(zone);
            return;
        }

        // If only one piece remains, update the existing zone record
        if (remainingPolys.size() == 1) {
            Polygon polygon = remainingPolys.getFirst();
            zone.setPolygon(polygon);
            zone.setLastUpdated(OffsetDateTime.now());
            zoneRepository.save(zone);
        } else {
            // If multiple pieces remain (MultiPolygon), delete original and save pieces.
            zoneRepository.delete(zone);
            for (Polygon piece : remainingPolys) {
                Zone newZone = new Zone();
                newZone.setOwner(zone.getOwner());
                newZone.setPolygon(piece);
                newZone.setCreatedAt(OffsetDateTime.now());
                newZone.setLastUpdated(OffsetDateTime.now());
                zoneRepository.save(newZone);
            }
        }
    }

    /**
     * Merge the conquered area into the user's existing zones and compute areaGained.
     */
    private void mergeConqueredIntoUserZones(List<Zone> myZones, Geometry conquered, User owner, Run run) {
        // --- 1. Compute Old Area (Used for Area Gained Calculation) ---
        double oldArea = 0.0;
        // NOTE: We rely on extractValidPolygons to filter out tiny pieces before summing the old area.
        for (Zone z : myZones) {
            List<Polygon> validPolys = extractValidPolygons(fixGeometry(z.getPolygon()));
            for (Polygon p : validPolys) {
                Geometry proj = GeometryProjectionUtil.toUTM(p);
                oldArea += proj.getArea();
            }
        }

        if (conquered == null || conquered.isEmpty()) {
            run.setAreaGained(0f);
            run.setDistance(0f);
            zoneRepository.deleteAll(myZones); // Delete zones even if no gain, as they were handled.
            return;
        }

        // --- 2. Calculate Merged Geometry ---

        // Start with the conquered area (already filtered for slivers by extractValidPolygons in updateZonesForRun)
        Geometry merged = fixGeometry(conquered);

        // Union with existing user zones, filtering each zone first
        for (Zone z : myZones) {
            // FIX: Use extractValidPolygons to ensure only substantial pieces are included in the union
            List<Polygon> validPolys = extractValidPolygons(fixGeometry(z.getPolygon()));
            for (Polygon p : validPolys) {
                merged = fixGeometry(merged.union(p));
            }
        }

        // --- 3. Persistence and Cleanup ---

        // Remove old zones from DB
        zoneRepository.deleteAll(myZones);

        // Persist merged geometry as valid polygons (filters final slivers)
        List<Polygon> polygonsToSave = extractValidPolygons(merged);
        for (Polygon poly : polygonsToSave) {
            saveZone(owner, poly);
        }

        // --- 4. Compute New Area and Metrics ---

        Geometry projectedMerged = GeometryProjectionUtil.toUTM(merged);
        double newArea = projectedMerged.getArea(); // Use getArea() directly on the merged geometry

        double areaGained = newArea - oldArea;
        if (areaGained < AREA_EPSILON) {
            areaGained = 0;
        }

        run.setAreaGained((float) areaGained);

        // Compute run perimeter
        Geometry projectedRun = GeometryProjectionUtil.toUTM(run.getPolygon());
        double runDistance = 0;

        if (projectedRun instanceof Polygon rpoly) {
            runDistance = rpoly.getExteriorRing().getLength();
        } else if (projectedRun instanceof MultiPolygon mp) {
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                // Perimeter of a run is just the length of its boundary (often a single ring)
                if (mp.getGeometryN(i) instanceof Polygon p) {
                    runDistance += p.getExteriorRing().getLength();
                }
            }
        }

        run.setDistance((float) runDistance);
    }


    // Save a Zone record (helper function)
    private void saveZone(User owner, Polygon polygon) {
        Geometry projected = GeometryProjectionUtil.toUTM(polygon);
        double area = projected.getArea();

        // Ignore tiny slivers created by GPS precision or geometry ops
        if (area < AREA_EPSILON) {
            return; // do not save this zone
        }

        Zone zone = new Zone();
        zone.setOwner(owner);
        zone.setPolygon(polygon);
        zone.setCreatedAt(OffsetDateTime.now());
        zone.setLastUpdated(OffsetDateTime.now());
        zoneRepository.save(zone);
    }


    // Fix simple topology problems (buffer(0) trick)
    private Geometry fixGeometry(Geometry geom) {
        if (geom == null) return geom;
        if (!geom.isValid()) {
            geom = geom.buffer(0);
        }
        return geom;
    }

    // Extract Polygon pieces from any geometry (Polygon, MultiPolygon or GeometryCollection)
    private List<Polygon> extractPolygons(Geometry geom) {
        List<Polygon> polys = new ArrayList<>();
        if (geom == null || geom.isEmpty()) return polys;

        if (geom instanceof Polygon p) {
            polys.add(p);
            return polys;
        }

        // Handle MultiPolygon and GeometryCollection iteratively
        if (geom instanceof GeometryCollection || geom instanceof MultiPolygon) {
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                Geometry g = geom.getGeometryN(i);
                if (g instanceof Polygon p) {
                    polys.add(p);
                } else if (g instanceof MultiPolygon mp) {
                    // Recurse for nested MultiPolygons if necessary, but typically JTS flattens MultiPolygon elements.
                    for (int j = 0; j < mp.getNumGeometries(); j++) {
                        Geometry g2 = mp.getGeometryN(j);
                        if (g2 instanceof Polygon p2) polys.add(p2);
                    }
                }
            }
        }

        return polys;
    }

    private List<Polygon> extractValidPolygons(Geometry geom) {
        List<Polygon> polys = extractPolygons(geom);
        List<Polygon> valid = new ArrayList<>();
        for (Polygon p : polys) {
            Geometry proj = GeometryProjectionUtil.toUTM(p);
            // Ignore tiny slivers
            if (proj.getArea() >= AREA_EPSILON) {
                valid.add(p);
            }
        }
        return valid;
    }
}