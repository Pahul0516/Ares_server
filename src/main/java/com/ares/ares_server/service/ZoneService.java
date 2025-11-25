package com.ares.ares_server.service;

import com.ares.ares_server.domain.Run;
import com.ares.ares_server.domain.User;
import com.ares.ares_server.domain.Zone;
import com.ares.ares_server.repository.ZoneRepository;
import com.ares.ares_server.utils.GeometryProjectionUtil;
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

        Geometry searchGeom = GeometryProjectionUtil.bufferInMeters(runGeom, 5);

        List<Zone> touchingZones = zoneRepository.findZonesIntersecting(searchGeom);

        var myZones = touchingZones.stream()
                .filter(z -> z.getOwner().getId().equals(run.getOwner().getId()))
                .toList();

        var otherZones = touchingZones.stream()
                .filter(z -> !z.getOwner().getId().equals(run.getOwner().getId()))
                .toList();

        for (Zone z : otherZones) {
            subtractRunFromZone(z, runGeom);
        }

        mergeZonesForUser(myZones, runGeom, run.getOwner(), run);

    }

    private void subtractRunFromZone(Zone zone, Geometry runGeom) {
        Geometry result = zone.getPolygon().difference(runGeom);

        if (result.isEmpty()) {
            zoneRepository.delete(zone);
            return;
        }

        if (result instanceof Polygon polygon) {
            zone.setPolygon(polygon);
            zone.setLastUpdated(OffsetDateTime.now());
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
                newZone.setCreatedAt(OffsetDateTime.now());
                newZone.setLastUpdated(OffsetDateTime.now());

                zoneRepository.save(newZone);
            }
        }
    }

    private void mergeZonesForUser(List<Zone> myZones, Geometry runGeom, User owner, Run run) {

        double oldArea = myZones.stream()
                .mapToDouble(z -> z.getPolygon().getArea())
                .sum();

        Geometry merged = runGeom;
        for (Zone z : myZones) {
            merged = merged.union(z.getPolygon());
        }

        double newArea = merged.getArea();

        double gained = Math.max(0, newArea - oldArea);
        run.setAreaGained((float) gained);

        zoneRepository.deleteAll(myZones);

        Zone finalZone = new Zone();
        finalZone.setOwner(owner);
        finalZone.setPolygon((Polygon) merged);
        finalZone.setCreatedAt(OffsetDateTime.now());
        finalZone.setLastUpdated(OffsetDateTime.now());

        zoneRepository.save(finalZone);
    }

}
