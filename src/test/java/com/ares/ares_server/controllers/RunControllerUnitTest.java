package com.ares.ares_server.controllers;

import com.ares.ares_server.dto.mappers.RunMapper;
import com.ares.ares_server.dto.RunDTO;
import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.domain.Run;
import com.ares.ares_server.domain.User;
import com.ares.ares_server.repository.RunRepository;
import com.ares.ares_server.service.ZoneService;
import com.ares.ares_server.utils.GeometryProjectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class RunControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private RunRepository runRepository;

    @Mock
    private RunMapper runMapper;

    @InjectMocks
    private RunController runController;

    @Mock
    private ZoneService zoneService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final GeometryFactory geometryFactory = new GeometryFactory();

    private User owner;
    private UserDTO ownerDto;
    private RunDTO inputDto;

    private Polygon createClosedPolygon() {
        Coordinate[] coords = {
                new Coordinate(0, 0), new Coordinate(1, 0),
                new Coordinate(1, 1), new Coordinate(0, 1),
                new Coordinate(0, 0) // Closed
        };
        return geometryFactory.createPolygon(coords);
    }

    private Polygon createUnclosedPolygonForTesting() {
        Coordinate[] coords = {
                new Coordinate(0, 0), new Coordinate(1, 0),
                new Coordinate(1, 1), new Coordinate(0, 1),
        };
        return geometryFactory.createPolygon(coords);
    }

    private Map<String, Object> polygonToGeoJson(Polygon polygon) {
        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "Polygon");

        List<List<List<Double>>> coordinates = new ArrayList<>();
        List<List<Double>> ring = new ArrayList<>();

        for (Coordinate c : polygon.getCoordinates()) {
            ring.add(Arrays.asList(c.x, c.y));
        }
        coordinates.add(ring);

        geoJson.put("coordinates", coordinates);
        return geoJson;
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(runController, "runRepository", runRepository);
        ReflectionTestUtils.setField(runController, "runMapper", runMapper);
        ReflectionTestUtils.setField(runController, "zoneService", zoneService);
        mockMvc = MockMvcBuilders.standaloneSetup(runController).build();

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setUsername("testuser");
        owner.setEmail("test@example.com");

        ownerDto = new UserDTO(new UUID(1, 3), owner.getUsername(), owner.getEmail(), null);
        inputDto = new RunDTO(null, OffsetDateTime.now(), ownerDto, 5f, 10f, null,OffsetDateTime.now().getSecond());
    }

    @Test
    void createRun_success_closedPolygon() throws Exception {
        Polygon runPolygon = createClosedPolygon();
        inputDto = new RunDTO(
                null,
                OffsetDateTime.now(),
                ownerDto,
                5f,
                10f,
                polygonToGeoJson(runPolygon),
                OffsetDateTime.now().getSecond()
        );

        Run runEntity = new Run();
        runEntity.setOwner(owner);
        runEntity.setPolygon(runPolygon);

        Run savedRun = new Run();
        savedRun.setId(1L);
        savedRun.setOwner(owner);
        savedRun.setPolygon(runPolygon);
        savedRun.setAreaGained(15f);

        RunDTO resultDto = new RunDTO(1L, inputDto.getCreatedAt(), ownerDto, 5f, 15f, polygonToGeoJson(runPolygon), inputDto.getDuration());

        try (MockedStatic<GeometryProjectionUtil> mockedUtil = mockStatic(GeometryProjectionUtil.class)) {
            mockedUtil.when(() -> GeometryProjectionUtil.bufferInMeters(any(Geometry.class), anyDouble()))
                    .thenAnswer(invocation -> {
                        Geometry geom = invocation.getArgument(0);
                        return geom.buffer(10); // simple valid buffer
                    });

            when(runMapper.fromDto(any(RunDTO.class))).thenReturn(runEntity);
            when(runRepository.save(any(Run.class))).thenReturn(savedRun);
            when(runMapper.toDto(savedRun)).thenReturn(resultDto);

            mockMvc.perform(post("/api/runs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.areaGained").value(15.0));

            verify(runMapper).fromDto(any(RunDTO.class));
            verify(zoneService).updateZonesForRun(any(Run.class));
            verify(runRepository, times(2)).save(any(Run.class));
            verify(runMapper).toDto(savedRun);
        }
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
                .andExpect(jsonPath("$.id").value(1));

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
                .andExpect(jsonPath("$.length()").value(2));

        verify(runRepository).findByOwnerId(owner.getId());
        verify(runMapper).toDto(r1);
        verify(runMapper).toDto(r2);
    }

    @Test
    void updateRun_success() throws Exception {
        Long runId = 1L;
        Float newDistance = 10f;
        RunDTO updatedDto = new RunDTO(null, null, null, newDistance, null, null, null);

        Run entityToSave = new Run(); entityToSave.setOwner(owner); entityToSave.setDistance(updatedDto.getDistance());

        Run savedEntity = new Run(); savedEntity.setId(runId); savedEntity.setDistance(newDistance);
        RunDTO returnedDto = new RunDTO(runId, null, null, newDistance, null, null, null);

        when(runRepository.existsById(runId)).thenReturn(true);
        when(runMapper.fromDto(updatedDto)).thenReturn(entityToSave);
        when(runRepository.save(any(Run.class))).thenReturn(savedEntity);
        when(runMapper.toDto(savedEntity)).thenReturn(returnedDto);

        mockMvc.perform(put("/api/runs/" + runId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(runId))
                .andExpect(jsonPath("$.distance").value(newDistance));

        verify(runRepository).existsById(runId);
        verify(runMapper).fromDto(updatedDto);
        verify(runRepository).save(any(Run.class));
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