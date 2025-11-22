package com.ares.ares_server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneDTO {
    private Long id;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastUpdated;
    @Schema(description = "GeoJSON polygon object", example = "{\"type\":\"Polygon\",\"coordinates\":[[[23.6,46.7],[23.7,46.8]]]}")
    private Map<String, Object> polygon;
    private Double area;
    private UserDTO owner;
}