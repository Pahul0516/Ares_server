package com.ares.ares_server.controllers;

import com.ares.ares_server.dto.mappers.ZoneMapper;
import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.dto.ZoneDTO;
import com.ares.ares_server.domain.Zone;
import com.ares.ares_server.domain.User;
import com.ares.ares_server.repository.ZoneRepository;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ZoneControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private ZoneMapper zoneMapper;

    @InjectMocks
    private ZoneController zoneController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).registerModule(new JtsModule());

    private final GeometryFactory geometryFactory = new GeometryFactory();

    private User owner;
    private UserDTO ownerDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(zoneController, "zoneRepository", zoneRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(zoneController).build();

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setUsername("owner1");
        owner.setEmail("owner@example.com");

        ownerDto = new UserDTO(owner.getUsername(), owner.getEmail(), null);

    }


    private Zone createSampleZone(Long id) {
        Polygon polygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 0),
                new Coordinate(0, 0)
        });

        Zone zone = new Zone();
        zone.setId(id);
        zone.setPolygon(polygon);
        zone.setOwner(owner);
        zone.setCreatedAt(OffsetDateTime.now());
        zone.setLastUpdated(OffsetDateTime.now());
        zone.setArea(100.0);
        return zone;
    }

    private ZoneDTO createSampleZoneDTO(Long id) {
        ZoneDTO dto = new ZoneDTO();
        dto.setId(id);
        dto.setOwner(ownerDto);
        dto.setCreatedAt(OffsetDateTime.now());
        dto.setLastUpdated(OffsetDateTime.now());
        dto.setArea(100.0);
        dto.setPolygon(null);
        return dto;
    }

    @Test
    void createZone_success() throws Exception {
        Zone zone = createSampleZone(1L);
        ZoneDTO inputDto = createSampleZoneDTO(1L);

        when(zoneMapper.fromDto(inputDto)).thenReturn(zone);
        when(zoneMapper.fromDto(any(ZoneDTO.class))).thenReturn(zone);
        when(zoneRepository.save(zone)).thenReturn(zone);
        when(zoneMapper.toDto(zone)).thenReturn(inputDto);

        mockMvc.perform(post("/api/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(zone.getId()))
                .andExpect(jsonPath("$.area").value(zone.getArea()));

        verify(zoneMapper).fromDto(any(ZoneDTO.class));
        verify(zoneRepository).save(zone);
        verify(zoneMapper).toDto(zone);
    }

    @Test
    void getAllZones_success() throws Exception {
        Zone zone1 = createSampleZone(1L);
        Zone zone2 = createSampleZone(2L);
        ZoneDTO dto1 = createSampleZoneDTO(1L);
        ZoneDTO dto2 = createSampleZoneDTO(2L);

        when(zoneRepository.findAll()).thenReturn(List.of(zone1, zone2));
        when(zoneMapper.toDto(zone1)).thenReturn(dto1);
        when(zoneMapper.toDto(zone2)).thenReturn(dto2);

        mockMvc.perform(get("/api/zones")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(zoneRepository).findAll();
        verify(zoneMapper).toDto(zone1);
        verify(zoneMapper).toDto(zone2);
    }

    @Test
    void getZoneById_found() throws Exception {
        Zone zone = createSampleZone(1L);
        ZoneDTO dto = createSampleZoneDTO(1L);

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(zoneMapper.toDto(zone)).thenReturn(dto);

        mockMvc.perform(get("/api/zones/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(zoneRepository).findById(1L);
        verify(zoneMapper).toDto(zone);
    }

    @Test
    void getZoneById_notFound() throws Exception {
        when(zoneRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/zones/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(zoneRepository).findById(99L);
    }

    @Test
    void updateZone_success() throws Exception {
        ZoneDTO inputDto = createSampleZoneDTO(null);
        Zone zoneEntity = createSampleZone(1L);
        ZoneDTO resultDto = createSampleZoneDTO(1L);

        when(zoneRepository.existsById(1L)).thenReturn(true);
        when(zoneMapper.fromDto(any(ZoneDTO.class))).thenReturn(zoneEntity);
        when(zoneRepository.save(any(Zone.class))).thenReturn(zoneEntity);
        when(zoneMapper.toDto(any(Zone.class))).thenReturn(resultDto);

        mockMvc.perform(put("/api/zones/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(zoneRepository).existsById(1L);
        verify(zoneMapper).fromDto(any(ZoneDTO.class));
        verify(zoneRepository).save(zoneEntity);
        verify(zoneMapper).toDto(zoneEntity);
    }

    @Test
    void updateZone_notFound() throws Exception {
        ZoneDTO inputDto = createSampleZoneDTO(null);
        when(zoneRepository.existsById(1L)).thenReturn(false);

        mockMvc.perform(put("/api/zones/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound());

        verify(zoneRepository).existsById(1L);
        verify(zoneMapper, never()).fromDto(any());
        verify(zoneRepository, never()).save(any());
    }

    @Test
    void deleteZone_success() throws Exception {
        when(zoneRepository.existsById(1L)).thenReturn(true);
        doNothing().when(zoneRepository).deleteById(1L);

        mockMvc.perform(delete("/api/zones/1"))
                .andExpect(status().isNoContent());

        verify(zoneRepository).existsById(1L);
        verify(zoneRepository).deleteById(1L);
    }

    @Test
    void deleteZone_notFound() throws Exception {
        when(zoneRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/zones/99"))
                .andExpect(status().isNotFound());

        verify(zoneRepository).existsById(99L);
        verify(zoneRepository, never()).deleteById(any());
    }

    @Test
    void getZonesByOwner_success() throws Exception {
        Zone zone1 = createSampleZone(1L);
        Zone zone2 = createSampleZone(2L);

        ZoneDTO dto1 = createSampleZoneDTO(1L);
        ZoneDTO dto2 = createSampleZoneDTO(2L);

        when(zoneRepository.findByOwnerId(owner.getId())).thenReturn(List.of(zone1, zone2));
        when(zoneMapper.toDto(zone1)).thenReturn(dto1);
        when(zoneMapper.toDto(zone2)).thenReturn(dto2);

        mockMvc.perform(get("/api/zones/owner/" + owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(zoneRepository).findByOwnerId(owner.getId());
        verify(zoneMapper).toDto(zone1);
        verify(zoneMapper).toDto(zone2);
    }
}