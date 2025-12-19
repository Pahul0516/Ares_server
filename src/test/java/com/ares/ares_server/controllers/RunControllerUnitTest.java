package com.ares.ares_server.controllers;

import com.ares.ares_server.dto.RunDTO;
import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.exceptions.RunDoesNotExistException;
import com.ares.ares_server.service.RunService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RunControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private RunService runService;

    @InjectMocks
    private RunController runController;

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    private UserDTO ownerDto;
    private RunDTO runDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(runController)
                .setControllerAdvice(new GlobalExceptionHandler()) // register your advice
                .build();

        ownerDto = new UserDTO(
                UUID.randomUUID(),
                "testuser",
                "test@example.com",
                null
        );

        runDto = new RunDTO(
                1L,
                OffsetDateTime.now(),
                ownerDto,
                5f,
                15f,
                Map.of("type", "Polygon"),
                120
        );
    }

    @Test
    void createRun_success() throws Exception {
        when(runService.createRun(any(RunDTO.class))).thenReturn(runDto);

        mockMvc.perform(post("/api/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(runDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.areaGained").value(15.0));

        verify(runService).createRun(any(RunDTO.class));
    }

    @Test
    void getAllRuns_success() throws Exception {
        when(runService.getAllRuns()).thenReturn(List.of(runDto));

        mockMvc.perform(get("/api/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(runService).getAllRuns();
    }

    @Test
    void getRunById_success() throws Exception {
        when(runService.getRunById(1L)).thenReturn(runDto);

        mockMvc.perform(get("/api/runs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(runService).getRunById(1L);
    }

    @Test
    void getRunById_notFound() throws Exception {
        when(runService.getRunById(999L))
                .thenThrow(new RunDoesNotExistException("Run with id 999 not found"));

        mockMvc.perform(get("/api/runs/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Run with id 999 not found"));

        verify(runService).getRunById(999L);
    }

    @Test
    void getRunsByOwner_success() throws Exception {
        UUID ownerId = ownerDto.getId();

        when(runService.getRunsByOwner(ownerId)).thenReturn(List.of(runDto));

        mockMvc.perform(get("/api/runs/owner/" + ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(runService).getRunsByOwner(ownerId);
    }

    @Test
    void getRunsByOwnerUsername_success() throws Exception {
        when(runService.getRunsByOwnerUsername("testuser"))
                .thenReturn(List.of(runDto));

        mockMvc.perform(get("/api/runs/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(runService).getRunsByOwnerUsername("testuser");
    }

    @Test
    void updateRun_success() throws Exception {
        when(runService.updateRun(eq(1L), any(RunDTO.class)))
                .thenReturn(runDto);

        mockMvc.perform(put("/api/runs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(runDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(runService).updateRun(eq(1L), any(RunDTO.class));
    }

    @Test
    void deleteRun_success() throws Exception {
        doNothing().when(runService).deleteRun(1L);

        mockMvc.perform(delete("/api/runs/1"))
                .andExpect(status().isNoContent());

        verify(runService).deleteRun(1L);
    }
}
