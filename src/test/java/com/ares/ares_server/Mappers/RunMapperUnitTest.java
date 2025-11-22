package com.ares.ares_server.Mappers;

import com.ares.ares_server.DTOs.Mappers.RunMapper;
import com.ares.ares_server.DTOs.RunDTO;
import com.ares.ares_server.Domain.Run;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
class RunMapperUnitTest {

    @Autowired
    private RunMapper runMapper;

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

        Run run = new Run();
        run.setId(1L);
        run.setPolygon(polygon);

        RunDTO dto = runMapper.toDto(run);

        assertNotNull(dto.getPolygon());
        assertEquals("Polygon", dto.getPolygon().get("type"));
    }

    @Test
    void toDto_mapsGeometryToGeoJson_null() {
        Run run = new Run();
        run.setPolygon(null);

        RunDTO dto = runMapper.toDto(run);

        assertNull(dto.getPolygon());

        Run run2 = null;

        RunDTO dto2 = runMapper.toDto(run2);

        assertNull(dto2);
    }



    @Test
    void fromDto_mapsGeoJsonToGeometry() {
        Map<String, Object> geoJson = Map.of(
                "type", "Polygon",
                "coordinates", new double[][][]{
                        {
                                {0, 0},
                                {0, 1},
                                {1, 1},
                                {1, 0},
                                {0, 0}
                        }
                }
        );

        RunDTO dto = new RunDTO();
        dto.setPolygon(geoJson);

        Run run = runMapper.fromDto(dto);

        assertNotNull(run.getPolygon());
        assertEquals("Polygon", run.getPolygon().getGeometryType());
    }

    @Test
    void fromDto_mapsGeoJsonToGeometry_null() {
        RunDTO dto = new RunDTO();
        dto.setPolygon(null);

        Run run = runMapper.fromDto(dto);

        assertNull(run.getPolygon());

        RunDTO dto2 = null;

        Run run2 = runMapper.fromDto(dto2);

        assertNull(run2);
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

        Map<String, Object> result = runMapper.geometryToGeoJson(poly);

        assertEquals("Polygon", result.get("type"));
    }

    @Test
    void geometryToGeoJson_throwsRuntimeException_onInvalidGeometry() {
        Geometry badGeometry = mock(Geometry.class);

        assertThrows(RuntimeException.class, () -> {
            runMapper.geometryToGeoJson(badGeometry);
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

        Polygon polygon = runMapper.geoJsonToGeometry(geoJson);

        assertEquals("Polygon", polygon.getGeometryType());
    }

    @Test
    void geoJsonToGeometry_throwsRuntimeException_onInvalidGeoJson() {
        Map<String, Object> invalid = Map.of(
                "type", "NotAGeometry",
                "coordinates", 123
        );

        assertThrows(RuntimeException.class, () -> {
            runMapper.geoJsonToGeometry(invalid);
        });
    }
}