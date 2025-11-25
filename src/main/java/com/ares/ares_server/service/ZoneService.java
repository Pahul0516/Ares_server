package com.ares.ares_server.service;

import com.ares.ares_server.domain.Run;
import com.ares.ares_server.domain.User;
import com.ares.ares_server.domain.Zone;
import com.ares.ares_server.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;

    @Transactional
    public void updateZonesForRun(Run run) {
        Geometry runGeom = run.getPolygon();

        List<Zone> touchingZones = zoneRepository.findZonesIntersecting(runGeom);

        // Split zones into friendly and foreign
        var myZones = touchingZones.stream()
                .filter(z -> z.getOwner().getId().equals(run.getOwner().getId()))
                .toList();

        var otherZones = touchingZones.stream()
                .filter(z -> !z.getOwner().getId().equals(run.getOwner().getId()))
                .toList();

        // 1. Subtract from other users
        for (Zone z : otherZones) {
            subtractRunFromZone(z, runGeom);
        }

        // 2. Merge into current user's zones
        mergeZonesForUser(myZones, runGeom, run.getOwner());
    }

    private void subtractRunFromZone(Zone zone, Geometry runGeom) {
        Geometry result = zone.getPolygon().difference(runGeom);

        if (result.isEmpty()) {
            zoneRepository.delete(zone);
            return;
        }

        if (result instanceof Polygon polygon) {
            zone.setPolygon(polygon);
            zone.setArea(polygon.getArea());
            zoneRepository.save(zone);
            return;
        }

        if (result instanceof MultiPolygon multiPolygon) {
            zoneRepository.delete(zone);

            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Polygon piece = (Polygon) multiPolygon.getGeometryN(i);

                Zone newZone = new Zone();
                newZone.setOwner(zone.getOwner());
                newZone.setPolygon(piece);
                newZone.setArea(piece.getArea());
                newZone.setCreatedAt(OffsetDateTime.now());

                zoneRepository.save(newZone);
            }
        }
    }

    private void mergeZonesForUser(List<Zone> myZones, Geometry runGeom, User owner) {
        Geometry merged = runGeom;

        for (Zone z : myZones) {
            merged = merged.union(z.getPolygon());
        }

        for (Zone z : myZones) {
            zoneRepository.delete(z);
        }

        Zone finalZone = new Zone();
        finalZone.setOwner(owner);
        finalZone.setPolygon((Polygon) merged);
        finalZone.setArea(merged.getArea());
        finalZone.setCreatedAt(OffsetDateTime.now());

        zoneRepository.save(finalZone);
    }
}
