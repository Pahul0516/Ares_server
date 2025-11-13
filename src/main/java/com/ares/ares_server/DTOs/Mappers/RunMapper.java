package com.ares.ares_server.DTOs.Mappers;

import com.ares.ares_server.DTOs.RunDTO;
import com.ares.ares_server.Domain.Run;
import com.fasterxml.jackson.core.type.TypeReference;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.mapstruct.*;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface RunMapper {
    ObjectMapper objectMapper = new ObjectMapper();
    GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
    GeoJsonReader geoJsonReader = new GeoJsonReader();

    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "polygon", expression = "java(geometryToGeoJson(run.getPolygon()))")
    RunDTO toDto(Run run);

    @InheritInverseConfiguration
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "polygon", expression = "java(geoJsonToGeometry(dto.getPolygon()))")
    Run fromDto(RunDTO dto);

    default Map<String,Object> geometryToGeoJson(Geometry geom) {
        if (geom == null) return null;
        try {
            String json = geoJsonWriter.write(geom);
            return objectMapper.readValue(
                    json,
                    new TypeReference<Map<String, Object>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Geometry to GeoJSON", e);
        }
    }

    default Polygon geoJsonToGeometry(Map<String, Object> geoJson) {
        if (geoJson == null) return null;
        try {
            String json = objectMapper.writeValueAsString(geoJson);
            Geometry geom = geoJsonReader.read(json);
            return (Polygon) geom;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert GeoJSON to Geometry", e);
        }
    }
}