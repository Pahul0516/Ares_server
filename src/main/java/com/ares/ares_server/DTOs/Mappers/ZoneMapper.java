package com.ares.ares_server.DTOs.Mappers;

import com.ares.ares_server.DTOs.ZoneDTO;
import com.ares.ares_server.Domain.Zone;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

@Mapper(componentModel = "spring", uses={UserMapper.class})
public interface ZoneMapper {
    ObjectMapper objectMapper = new ObjectMapper();
    GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
    GeoJsonReader geoJsonReader = new GeoJsonReader();

    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "polygon", expression = "java(geometryToGeoJson(zone.getPolygon()))")
    ZoneDTO toDto(Zone zone);

    @InheritInverseConfiguration
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "polygon", expression = "java(geoJsonToGeometry(dto.getPolygon()))")
    Zone fromDto(ZoneDTO dto);

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