package com.ares.ares_server.Mappers;

import com.ares.ares_server.DTOs.Mappers.ZoneMapper;
import com.ares.ares_server.DTOs.ZoneDTO;
import com.ares.ares_server.Domain.Zone;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@SpringBootTest
class ZoneMapperUnitTest {

    @Autowired
    private ZoneMapper zoneMapper;

    @Test
    void toDto_mapsGeometryToGeoJson() {
        GeometryFactory factory = new GeometryFactory();
        Polygon polygon = factory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 0),
                new Coordinate(0, 0),
        });

        Zone zone = new Zone();
        zone.setId(1L);
        zone.setPolygon(polygon);
        zone.setArea(123.0);
        zone.setCreatedAt(OffsetDateTime.now());
        zone.setLastUpdated(OffsetDateTime.now());

        ZoneDTO dto = zoneMapper.toDto(zone);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertNotNull(dto.getPolygon());
        assertEquals("Polygon", dto.getPolygon().get("type"));
    }

    @Test
    void toDto_mapsGeometryToGeoJson_null() {
        Zone zone = new Zone();
        zone.setPolygon(null);

        ZoneDTO dto = zoneMapper.toDto(zone);

        assertNull(dto.getPolygon());

        Zone zone2 = null;

        ZoneDTO dto2 = zoneMapper.toDto(zone2);

        assertNull(dto2);
    }

    @Test
    void fromDto_mapsGeoJsonToGeometry() {
        Map<String, Object> geoJson = Map.of(
                "type", "Polygon",
                "coordinates", List.of(
                        List.of(
                                List.of(0.0, 0.0),
                                List.of(0.0, 1.0),
                                List.of(1.0, 1.0),
                                List.of(1.0, 0.0),
                                List.of(0.0, 0.0)
                        )
                )
        );

        ZoneDTO dto = new ZoneDTO();
        dto.setId(5L);
        dto.setArea(200.0);
        dto.setPolygon(geoJson);
        dto.setCreatedAt(OffsetDateTime.now());
        dto.setLastUpdated(OffsetDateTime.now());

        Zone zone = zoneMapper.fromDto(dto);

        assertNotNull(zone);
        assertEquals(5L, zone.getId());
        assertNotNull(zone.getPolygon());
        assertEquals("Polygon", zone.getPolygon().getGeometryType());
    }

    @Test
    void fromDto_mapsGeoJsonToGeometry_null() {
        ZoneDTO dto = new ZoneDTO();
        dto.setPolygon(null);

        Zone run = zoneMapper.fromDto(dto);

        assertNull(run.getPolygon());

        ZoneDTO dto2 = null;

        Zone zone2 = zoneMapper.fromDto(dto2);

        assertNull(zone2);
    }

    @Test
    void geometryToGeoJson_convertsCorrectly() {
        GeometryFactory factory = new GeometryFactory();
        Polygon poly = factory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1),
                new Coordinate(0, 0)
        });

        Map<String, Object> result = zoneMapper.geometryToGeoJson(poly);

        assertEquals("Polygon", result.get("type"));
    }

    @Test
    void geometryToGeoJson_throwsRuntimeException_onInvalidGeometry() {
        Geometry badGeometry = mock(Geometry.class);

        assertThrows(RuntimeException.class, () -> {
            zoneMapper.geometryToGeoJson(badGeometry);
        });
    }

    @Test
    void geoJsonToGeometry_convertsCorrectly() {
        Map<String, Object> geoJson = Map.of(
                "type", "Polygon",
                "coordinates", List.of(
                        List.of(
                                List.of(0.0, 0.0),
                                List.of(1.0, 0.0),
                                List.of(1.0, 1.0),
                                List.of(0.0, 1.0),
                                List.of(0.0, 0.0)
                        )
                )
        );

        Polygon polygon = zoneMapper.geoJsonToGeometry(geoJson);

        assertEquals("Polygon", polygon.getGeometryType());
    }

    @Test
    void geoJsonToGeometry_throwsRuntimeException_onInvalidGeoJson() {
        Map<String, Object> invalid = Map.of(
                "type", "NotAGeometry",
                "coordinates", 123
        );

        assertThrows(RuntimeException.class, () -> {
            zoneMapper.geoJsonToGeometry(invalid);
        });
    }
}