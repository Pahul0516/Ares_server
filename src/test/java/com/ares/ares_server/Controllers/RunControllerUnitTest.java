package com.ares.ares_server.Controllers;

import com.ares.ares_server.DTOs.Mappers.RunMapper;
import com.ares.ares_server.DTOs.RunDTO;
import com.ares.ares_server.DTOs.UserDTO;
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

    @Mock
    private RunMapper runMapper;

    @InjectMocks
    private RunController runController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private User owner;
    private UserDTO ownerDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(runController, "runRepository", runRepository);
        ReflectionTestUtils.setField(runController, "runMapper", runMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(runController).build();

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setUsername("testuser");
        owner.setEmail("test@example.com");

        ownerDto = new UserDTO(owner.getId(), owner.getUsername(), owner.getEmail(), null);
    }

    @Test
    void createRun_success() throws Exception {
        UserDTO ownerDto = new UserDTO(owner.getId(), owner.getUsername(), owner.getEmail(), null);
        RunDTO inputDto = new RunDTO(null, OffsetDateTime.now(), ownerDto, 5f, 10f, null, Instant.ofEpochSecond(1800));

        Run saved = new Run();
        saved.setId(1L);
        saved.setOwner(owner);
        saved.setDistance(inputDto.getDistance());
        saved.setAreaGained(inputDto.getAreaGained());
        saved.setCreatedAt(inputDto.getCreatedAt());
        saved.setDuration(inputDto.getDuration());

        RunDTO resultDto = new RunDTO(1L, inputDto.getCreatedAt(), ownerDto, 5f, 10f, null, inputDto.getDuration());

        when(runMapper.fromDto(any(RunDTO.class))).thenReturn(saved);
        when(runRepository.save(any(Run.class))).thenReturn(saved);
        when(runMapper.toDto(any(Run.class))).thenReturn(resultDto);

        mockMvc.perform(post("/api/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.distance").value(5.0))
                .andExpect(jsonPath("$.areaGained").value(10.0));

        verify(runMapper).fromDto(any(RunDTO.class));
        verify(runRepository).save(any(Run.class));
        verify(runMapper).toDto(any(Run.class));
    }

    @Test
    void getAllRuns_returnsList() throws Exception {
        Run r1 = new Run(); r1.setId(1L); r1.setOwner(owner);
        Run r2 = new Run(); r2.setId(2L); r2.setOwner(owner);

        RunDTO dto1 = new RunDTO(1L, null, null, null, null, null, null);
        RunDTO dto2 = new RunDTO(2L, null, null, null, null, null, null);

        when(runRepository.findAll()).thenReturn(Arrays.asList(r1, r2));
        when(runMapper.toDto(r1)).thenReturn(dto1);
        when(runMapper.toDto(r2)).thenReturn(dto2);

        mockMvc.perform(get("/api/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(runRepository).findAll();
        verify(runMapper).toDto(r1);
        verify(runMapper).toDto(r2);
    }

    @Test
    void getRunById_found() throws Exception {
        Run run = new Run(); run.setId(1L); run.setOwner(owner);
        RunDTO dto = new RunDTO(1L, null, ownerDto, null, null, null, null);

        when(runRepository.findById(1L)).thenReturn(Optional.of(run));
        when(runMapper.toDto(run)).thenReturn(dto);

        mockMvc.perform(get("/api/runs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.owner.id").value(owner.getId().toString()));

        verify(runRepository).findById(1L);
        verify(runMapper).toDto(run);
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

        RunDTO dto1 = new RunDTO(1L, null, ownerDto, null, null, null, null);
        RunDTO dto2 = new RunDTO(2L, null, ownerDto, null, null, null, null);

        when(runRepository.findByOwnerId(owner.getId())).thenReturn(Arrays.asList(r1, r2));
        when(runMapper.toDto(r1)).thenReturn(dto1);
        when(runMapper.toDto(r2)).thenReturn(dto2);

        mockMvc.perform(get("/api/runs/owner/" + owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].owner.id").value(owner.getId().toString()))
                .andExpect(jsonPath("$[1].owner.id").value(owner.getId().toString()));

        verify(runRepository).findByOwnerId(owner.getId());
        verify(runMapper).toDto(r1);
        verify(runMapper).toDto(r2);
    }

    @Test
    void updateRun_success() throws Exception {
        RunDTO updatedDto = new RunDTO(null, null, null, 10f, null, null, null);
        Run entity = new Run(); entity.setOwner(owner); entity.setDistance(updatedDto.getDistance());

        Run savedEntity = new Run(); savedEntity.setId(1L); savedEntity.setDistance(10f);
        RunDTO returnedDto = new RunDTO(1L, null, null, 10f, null, null, null);

        when(runRepository.existsById(1L)).thenReturn(true);
        when(runMapper.fromDto(updatedDto)).thenReturn(entity);
        when(runRepository.save(entity)).thenReturn(savedEntity);
        when(runMapper.toDto(savedEntity)).thenReturn(returnedDto);

        mockMvc.perform(put("/api/runs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.distance").value(10.0));

        verify(runRepository).existsById(1L);
        verify(runMapper).fromDto(updatedDto);
        verify(runRepository).save(entity);
        verify(runMapper).toDto(savedEntity);
    }

    @Test
    void updateRun_notFound() throws Exception {
        RunDTO updatedDto = new RunDTO(null, null, null, null, null, null, null);

        when(runRepository.existsById(2L)).thenReturn(false);

        mockMvc.perform(put("/api/runs/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isNotFound());

        verify(runRepository).existsById(2L);
        verify(runMapper, never()).fromDto(any());
        verify(runRepository, never()).save(any());
    }

    @Test
    void deleteRun_success() throws Exception {
        when(runRepository.existsById(1L)).thenReturn(true);
        doNothing().when(runRepository).deleteById(1L);

        mockMvc.perform(delete("/api/runs/1"))
                .andExpect(status().isNoContent());

        verify(runRepository).existsById(1L);
        verify(runRepository).deleteById(1L);
    }

    @Test
    void deleteRun_notFound() throws Exception {
        when(runRepository.existsById(2L)).thenReturn(false);

        mockMvc.perform(delete("/api/runs/2"))
                .andExpect(status().isNotFound());

        verify(runRepository).existsById(2L);
        verify(runRepository, never()).deleteById(2L);
    }
}