package com.ares.ares_server.service;

import com.ares.ares_server.dto.mappers.RunMapper;
import com.ares.ares_server.dto.RunDTO;
import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.domain.Run;
import com.ares.ares_server.domain.User;
import com.ares.ares_server.exceptios.RunDoesNotExistException;
import com.ares.ares_server.repository.RunRepository;
import com.ares.ares_server.utils.GeometryProjectionUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RunServiceUnitTest {

    @Mock
    private RunRepository runRepository;

    @Mock
    private RunMapper runMapper;

    @InjectMocks
    private RunService runService;

    @Mock
    private ZoneService zoneService;

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
        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setUsername("testuser");
        owner.setEmail("test@example.com");

        ownerDto = new UserDTO(new UUID(1, 3), owner.getUsername(), owner.getEmail(), null);
        inputDto = new RunDTO(null, OffsetDateTime.now(), ownerDto, 5f, 10f, null,OffsetDateTime.now().getSecond());
    }

    @Test
    void createRun_success_closedPolygon(){
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

            RunDTO result = runService.createRun(inputDto);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(5f, result.getDistance());
            assertEquals(15f, result.getAreaGained());
            assertEquals(inputDto.getDuration(), result.getDuration());
            assertNotNull(result.getPolygon());


            verify(runMapper).fromDto(any(RunDTO.class));
            verify(zoneService).updateZonesForRun(any(Run.class));
            verify(runRepository, times(2)).save(any(Run.class));
            verify(runMapper).toDto(savedRun);
        }
    }

    @Test
    void getAllRuns_returnsList(){
        Run r1 = new Run(); r1.setId(1L); r1.setOwner(owner);
        Run r2 = new Run(); r2.setId(2L); r2.setOwner(owner);

        RunDTO dto1 = new RunDTO(1L, null, null, null, null, null, null);
        RunDTO dto2 = new RunDTO(2L, null, null, null, null, null, null);

        when(runRepository.findAll()).thenReturn(Arrays.asList(r1, r2));
        when(runMapper.toDto(r1)).thenReturn(dto1);
        when(runMapper.toDto(r2)).thenReturn(dto2);

        List<RunDTO> result =  runService.getAllRuns();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(runRepository).findAll();
        verify(runMapper).toDto(r1);
        verify(runMapper).toDto(r2);
    }

    @Test
    void getRunById_found(){
        Run run = new Run(); run.setId(1L); run.setOwner(owner);
        RunDTO dto = new RunDTO(1L, null, ownerDto, null, null, null, null);

        when(runRepository.findById(1L)).thenReturn(Optional.of(run));
        when(runMapper.toDto(run)).thenReturn(dto);

        RunDTO result = runService.getRunById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(ownerDto.getUsername(), result.getOwner().getUsername());

        verify(runRepository).findById(1L);
        verify(runMapper).toDto(run);
    }

    @Test
    void getRunById_notFound(){
        when(runRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RunDoesNotExistException.class, () -> runService.getRunById(1L));

        verify(runRepository).findById(1L);
    }

    @Test
    void getRunsByOwner_returnsList() {
        Run r1 = new Run(); r1.setId(1L); r1.setOwner(owner);
        Run r2 = new Run(); r2.setId(2L); r2.setOwner(owner);

        RunDTO dto1 = new RunDTO(1L, null, ownerDto, null, null, null, null);
        RunDTO dto2 = new RunDTO(2L, null, ownerDto, null, null, null, null);

        when(runRepository.findByOwnerId(owner.getId())).thenReturn(Arrays.asList(r1, r2));
        when(runMapper.toDto(r1)).thenReturn(dto1);
        when(runMapper.toDto(r2)).thenReturn(dto2);

        List<RunDTO> result = runService.getRunsByOwner(owner.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(runRepository).findByOwnerId(owner.getId());
        verify(runMapper).toDto(r1);
        verify(runMapper).toDto(r2);
    }

    @Test
    void getRunsByOwner_notFound() throws Exception {
        when(runRepository.findByOwnerId(owner.getId())).thenReturn(Collections.emptyList());

        assertThrows(RunDoesNotExistException.class, () -> runService.getRunsByOwner(owner.getId()));

        verify(runRepository).findByOwnerId(owner.getId());
    }

    @Test
    void getRunsByOwnerUsername_returnsList() {
        Run r1 = new Run(); r1.setId(1L); r1.setOwner(owner);
        Run r2 = new Run(); r2.setId(2L); r2.setOwner(owner);

        RunDTO dto1 = new RunDTO(1L, null, ownerDto, null, null, null, null);
        RunDTO dto2 = new RunDTO(2L, null, ownerDto, null, null, null, null);

        when(runRepository.findByOwnerUsername(owner.getUsername())).thenReturn(Arrays.asList(r1, r2));
        when(runMapper.toDto(r1)).thenReturn(dto1);
        when(runMapper.toDto(r2)).thenReturn(dto2);

        List<RunDTO> result = runService.getRunsByOwnerUsername(owner.getUsername());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(runRepository).findByOwnerUsername(owner.getUsername());
        verify(runMapper).toDto(r1);
        verify(runMapper).toDto(r2);
    }

    @Test
    void getRunsByOwnerUsername_notFound() {
        when(runRepository.findByOwnerUsername(owner.getUsername())).thenReturn(Collections.emptyList());

        assertThrows(RunDoesNotExistException.class, () -> runService.getRunsByOwnerUsername(owner.getUsername()));

        verify(runRepository).findByOwnerUsername(owner.getUsername());
    }

    @Test
    void updateRun_success() {
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

        RunDTO result = runService.updateRun(runId, updatedDto);

        assertNotNull(result);
        assertEquals(runId, result.getId());
        assertEquals(newDistance, result.getDistance());

        verify(runRepository).existsById(runId);
        verify(runMapper).fromDto(updatedDto);
        verify(runRepository).save(any(Run.class));
        verify(runMapper).toDto(savedEntity);
    }

    @Test
    void updateRun_notFound() {
        RunDTO updatedDto = new RunDTO(null, null, null, null, null, null, null);

        when(runRepository.existsById(2L)).thenReturn(false);

        assertThrows(RunDoesNotExistException.class, () -> runService.updateRun(2L, updatedDto));

        verify(runRepository).existsById(2L);
        verify(runMapper, never()).fromDto(any());
        verify(runRepository, never()).save(any());
    }

    @Test
    void deleteRun_success() {
        when(runRepository.existsById(1L)).thenReturn(true);
        doNothing().when(runRepository).deleteById(1L);

        runService.deleteRun(1L);

        verify(runRepository).existsById(1L);
        verify(runRepository).deleteById(1L);
    }

    @Test
    void deleteRun_notFound() {
        when(runRepository.existsById(2L)).thenReturn(false);

        assertThrows(RunDoesNotExistException.class, () -> runService.deleteRun(2L));

        verify(runRepository).existsById(2L);
        verify(runRepository, never()).deleteById(2L);
    }
}
