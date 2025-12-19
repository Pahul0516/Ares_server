package com.ares.ares_server.controllers;

import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.dto.ZoneDTO;
import com.ares.ares_server.exceptios.ZoneDoesNotExistException;
import com.ares.ares_server.service.ZoneService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ZoneControllerUnitTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ZoneController zoneController;

    @Mock
    private ZoneService zoneService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UserDTO ownerDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(zoneController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        ownerDto = new UserDTO(UUID.randomUUID(), "owner1", "owner@example.com", null);
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
        ZoneDTO inputDto = createSampleZoneDTO(null);
        ZoneDTO returnedDto = createSampleZoneDTO(1L);

        when(zoneService.createZone(any(ZoneDTO.class))).thenReturn(returnedDto);

        mockMvc.perform(post("/api/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.area").value(100.0));

        verify(zoneService).createZone(any(ZoneDTO.class));
    }

    @Test
    void getAllZones_success() throws Exception {
        ZoneDTO dto1 = createSampleZoneDTO(1L);
        ZoneDTO dto2 = createSampleZoneDTO(2L);

        when(zoneService.getAllZones()).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(zoneService).getAllZones();
    }

    @Test
    void getZoneById_found() throws Exception {
        ZoneDTO dto = createSampleZoneDTO(1L);

        when(zoneService.getZoneById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/zones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(zoneService).getZoneById(1L);
    }

    @Test
    void getZoneById_notFound() throws Exception {
        when(zoneService.getZoneById(999L))
                .thenThrow(new ZoneDoesNotExistException("Zone with id 999 not found"));

        mockMvc.perform(get("/api/zones/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Zone with id 999 not found"));

        verify(zoneService).getZoneById(999L);
    }

    @Test
    void getZonesByOwner_success() throws Exception {
        ZoneDTO dto1 = createSampleZoneDTO(1L);
        ZoneDTO dto2 = createSampleZoneDTO(2L);

        UUID ownerId = ownerDto.getId();

        when(zoneService.getZonesByOwner(ownerId)).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/zones/owner/" + ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(zoneService).getZonesByOwner(ownerId);
    }

    @Test
    void updateZone_success() throws Exception {
        ZoneDTO inputDto = createSampleZoneDTO(null);
        ZoneDTO returnedDto = createSampleZoneDTO(1L);

        when(zoneService.updateZone(eq(1L), any(ZoneDTO.class))).thenReturn(returnedDto);

        mockMvc.perform(put("/api/zones/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(zoneService).updateZone(eq(1L), any(ZoneDTO.class));
    }

    @Test
    void deleteZone_success() throws Exception {
        doNothing().when(zoneService).deleteZone(1L);

        mockMvc.perform(delete("/api/zones/1"))
                .andExpect(status().isNoContent());

        verify(zoneService).deleteZone(1L);
    }
}
