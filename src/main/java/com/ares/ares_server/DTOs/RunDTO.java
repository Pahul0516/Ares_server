package com.ares.ares_server.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunDTO {
    private Long id;
    private OffsetDateTime createdAt;
    private UserDTO owner;
    private Float distance;
    private Float areaGained;
    @Schema(description = "GeoJSON polygon object", example = "{\"type\":\"Polygon\",\"coordinates\":[[[23.6,46.7],[23.7,46.8]]]}")
    private Map<String, Object> polygon;
    private Instant duration;
}