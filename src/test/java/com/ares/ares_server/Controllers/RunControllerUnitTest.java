package com.ares.ares_server.Controllers;

import com.ares.ares_server.Domain.Run;
import com.ares.ares_server.Domain.User;
import com.ares.ares_server.Repository.RunRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RunControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private RunRepository runRepository;

    @InjectMocks
    private RunController runController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private User owner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(runController, "runRepository", runRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(runController).build();

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setUsername("testuser");
        owner.setEmail("test@example.com");
    }

    @Test
    void createRun_success() throws Exception {
        Run input = new Run();
        input.setOwner(owner);
        input.setDistance(5.0f);
        input.setAreaGained(10.0f);
        input.setCreatedAt(OffsetDateTime.now());
        input.setDuration(Instant.ofEpochSecond(1800));

        Run saved = new Run();
        saved.setId(1L);
        saved.setOwner(owner);
        saved.setDistance(input.getDistance());
        saved.setAreaGained(input.getAreaGained());
        saved.setCreatedAt(input.getCreatedAt());
        saved.setDuration(input.getDuration());

        when(runRepository.save(any(Run.class))).thenReturn(saved);

        mockMvc.perform(post("/api/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.owner.id").value(owner.getId().toString()));

        verify(runRepository).save(any(Run.class));
    }

    @Test
    void getAllRuns_returnsList() throws Exception {
        Run r1 = new Run(); r1.setId(1L); r1.setOwner(owner);
        Run r2 = new Run(); r2.setId(2L); r2.setOwner(owner);
        when(runRepository.findAll()).thenReturn(Arrays.asList(r1, r2));

        mockMvc.perform(get("/api/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(runRepository).findAll();
    }

    @Test
    void getRunById_found() throws Exception {
        Run r = new Run(); r.setId(1L); r.setOwner(owner);
        when(runRepository.findById(1L)).thenReturn(Optional.of(r));

        mockMvc.perform(get("/api/runs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.owner.id").value(owner.getId().toString()));

        verify(runRepository).findById(1L);
    }

    @Test
    void getRunById_notFound() throws Exception {
        when(runRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/runs/1"))
                .andExpect(status().isNotFound());

        verify(runRepository).findById(1L);
    }

    @Test
    void getRunsByOwner_returnsList() throws Exception {
        Run r1 = new Run(); r1.setId(1L); r1.setOwner(owner);
        Run r2 = new Run(); r2.setId(2L); r2.setOwner(owner);
        when(runRepository.findByOwnerId(owner.getId())).thenReturn(Arrays.asList(r1, r2));

        mockMvc.perform(get("/api/runs/owner/" + owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].owner.id").value(owner.getId().toString()))
                .andExpect(jsonPath("$[1].owner.id").value(owner.getId().toString()));

        verify(runRepository).findByOwnerId(owner.getId());
    }

    @Test
    void updateRun_success() throws Exception {
        Run updated = new Run();
        updated.setOwner(owner);
        updated.setDistance(10f);

        when(runRepository.existsById(1L)).thenReturn(true);
        when(runRepository.save(any(Run.class))).thenAnswer(invocation -> {
            Run arg = invocation.getArgument(0);
            arg.setId(1L);
            return arg;
        });

        mockMvc.perform(put("/api/runs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.distance").value(10.0));

        verify(runRepository).existsById(1L);
        verify(runRepository).save(any(Run.class));
    }

    @Test
    void updateRun_notFound() throws Exception {
        Run updated = new Run();
        updated.setOwner(owner);

        when(runRepository.existsById(2L)).thenReturn(false);

        mockMvc.perform(put("/api/runs/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound());

        verify(runRepository).existsById(2L);
        verify(runRepository, never()).save(any());
    }

    @Test
    void deleteRun_success_and_notFound() throws Exception {
        when(runRepository.existsById(1L)).thenReturn(true);
        doNothing().when(runRepository).deleteById(1L);

        mockMvc.perform(delete("/api/runs/1"))
                .andExpect(status().isNoContent());

        when(runRepository.existsById(2L)).thenReturn(false);

        mockMvc.perform(delete("/api/runs/2"))
                .andExpect(status().isNotFound());

        verify(runRepository).existsById(1L);
        verify(runRepository).deleteById(1L);
        verify(runRepository).existsById(2L);
        verify(runRepository, never()).deleteById(2L);
    }
}
