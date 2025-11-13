package com.ares.ares_server.Controllers;

import com.ares.ares_server.Domain.Zone;
import com.ares.ares_server.Domain.User;
import com.ares.ares_server.Repository.ZoneRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Coordinate;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ZoneControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private ZoneRepository zoneRepository;

    @InjectMocks
    private ZoneController zoneController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).registerModule(new JtsModule());

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(zoneController, "zoneRepository", zoneRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(zoneController).build();

    }


    private Zone createSampleZone(Long id) {
        Polygon polygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 0),
                new Coordinate(0, 0)
        });

        User owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setUsername("owner1");
        owner.setEmail("owner@example.com");

        Zone zone = new Zone();
        zone.setId(id);
        zone.setPolygon(null);
        zone.setOwner(owner);
        zone.setCreatedAt(OffsetDateTime.now());
        zone.setLastUpdated(OffsetDateTime.now());
        zone.setArea(100.0);
        return zone;
    }

    @Test
    void createZone_success() throws Exception {
        Zone zone = createSampleZone(1L);
        when(zoneRepository.save(any())).thenReturn(zone);

        mockMvc.perform(post("/api/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zone)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(zone.getId()))
                .andExpect(jsonPath("$.area").value(zone.getArea()));
    }

    @Test
    void getAllZones_success() throws Exception {
        Zone zone1 = createSampleZone(1L);
        Zone zone2 = createSampleZone(2L);

        when(zoneRepository.findAll()).thenReturn(List.of(zone1, zone2));

        mockMvc.perform(get("/api/zones")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getZoneById_found() throws Exception {
        Zone zone = createSampleZone(1L);
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));

        mockMvc.perform(get("/api/zones/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getZoneById_notFound() throws Exception {
        when(zoneRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/zones/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateZone_success() throws Exception {
        Zone zone = createSampleZone(1L);
        when(zoneRepository.existsById(1L)).thenReturn(true);
        when(zoneRepository.save(any())).thenReturn(zone);

        mockMvc.perform(put("/api/zones/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateZone_notFound() throws Exception {
        Zone zone = createSampleZone(1L);
        when(zoneRepository.existsById(1L)).thenReturn(false);

        mockMvc.perform(put("/api/zones/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zone)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteZone_success() throws Exception {
        when(zoneRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/zones/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteZone_notFound() throws Exception {
        when(zoneRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/zones/99"))
                .andExpect(status().isNotFound());
    }
}
