package com.ares.ares_server.service;

import com.ares.ares_server.dto.mappers.ZoneMapper;
import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.dto.ZoneDTO;
import com.ares.ares_server.domain.Zone;
import com.ares.ares_server.domain.User;
import com.ares.ares_server.exceptions.ZoneDoesNotExistException;
import com.ares.ares_server.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Coordinate;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ZoneServiceUnitTest {


    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private ZoneMapper zoneMapper;

    @InjectMocks
    private ZoneService zoneService;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    private User owner;
    private UserDTO ownerDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setUsername("owner1");
        owner.setEmail("owner@example.com");

        ownerDto = new UserDTO(owner.getId(), owner.getUsername(), owner.getEmail(), null);

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
    void createZone_success() {
        Zone zone = createSampleZone(1L);
        ZoneDTO inputDto = createSampleZoneDTO(1L);

        when(zoneMapper.fromDto(inputDto)).thenReturn(zone);
        when(zoneRepository.save(zone)).thenReturn(zone);
        when(zoneMapper.toDto(zone)).thenReturn(inputDto);

        ZoneDTO result = zoneService.createZone(inputDto);

        assertNotNull(result);
        assertEquals(zone.getId(), result.getId());
        assertEquals(zone.getArea(), result.getArea());

        verify(zoneMapper).fromDto(any(ZoneDTO.class));
        verify(zoneRepository).save(zone);
        verify(zoneMapper).toDto(zone);
    }

    @Test
    void getAllZones_success() {
        Zone zone1 = createSampleZone(1L);
        Zone zone2 = createSampleZone(2L);
        ZoneDTO dto1 = createSampleZoneDTO(1L);
        ZoneDTO dto2 = createSampleZoneDTO(2L);

        when(zoneRepository.findAll()).thenReturn(List.of(zone1, zone2));
        when(zoneMapper.toDto(zone1)).thenReturn(dto1);
        when(zoneMapper.toDto(zone2)).thenReturn(dto2);

        List<ZoneDTO> result = zoneService.getAllZones();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(dto1.getId(), result.get(0).getId());
        assertEquals(dto2.getId(), result.get(1).getId());

        verify(zoneRepository).findAll();
        verify(zoneMapper).toDto(zone1);
        verify(zoneMapper).toDto(zone2);
    }

    @Test
    void getZoneById_found() {
        Zone zone = createSampleZone(1L);
        ZoneDTO dto = createSampleZoneDTO(1L);

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(zoneMapper.toDto(zone)).thenReturn(dto);

        ZoneDTO result = zoneService.getZoneById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(zoneRepository).findById(1L);
        verify(zoneMapper).toDto(zone);
    }

    @Test
    void getZoneById_notFound() {
        when(zoneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ZoneDoesNotExistException.class, () -> zoneService.getZoneById(99L));

        verify(zoneRepository).findById(99L);
    }

    @Test
    void updateZone_success() {
        ZoneDTO inputDto = createSampleZoneDTO(null);
        Zone zoneEntity = createSampleZone(1L);
        ZoneDTO resultDto = createSampleZoneDTO(1L);

        when(zoneRepository.existsById(1L)).thenReturn(true);
        when(zoneMapper.fromDto(any(ZoneDTO.class))).thenReturn(zoneEntity);
        when(zoneRepository.save(any(Zone.class))).thenReturn(zoneEntity);
        when(zoneMapper.toDto(any(Zone.class))).thenReturn(resultDto);

        ZoneDTO result = zoneService.updateZone(1L, inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(zoneEntity.getId(), result.getId());
        assertEquals(zoneEntity.getArea(), result.getArea());
        assertEquals(zoneEntity.getCreatedAt(), result.getCreatedAt());

        verify(zoneRepository).existsById(1L);
        verify(zoneMapper).fromDto(any(ZoneDTO.class));
        verify(zoneRepository).save(zoneEntity);
        verify(zoneMapper).toDto(zoneEntity);
    }

    @Test
    void updateZone_notFound() {
        ZoneDTO inputDto = createSampleZoneDTO(null);
        when(zoneRepository.existsById(1L)).thenReturn(false);

        assertThrows(ZoneDoesNotExistException.class, () -> zoneService.updateZone(1L, inputDto));

        verify(zoneRepository).existsById(1L);
        verify(zoneMapper, never()).fromDto(any());
        verify(zoneRepository, never()).save(any());
    }

    @Test
    void deleteZone_success() {
        when(zoneRepository.existsById(1L)).thenReturn(true);
        doNothing().when(zoneRepository).deleteById(1L);

        zoneService.deleteZone(1L);

        verify(zoneRepository).existsById(1L);
        verify(zoneRepository).deleteById(1L);
    }

    @Test
    void deleteZone_notFound() {
        when(zoneRepository.existsById(99L)).thenReturn(false);

        assertThrows(ZoneDoesNotExistException.class, () -> zoneService.deleteZone(99L));

        verify(zoneRepository).existsById(99L);
        verify(zoneRepository, never()).deleteById(any());
    }

    @Test
    void getZonesByOwner_success() {
        Zone zone1 = createSampleZone(1L);
        Zone zone2 = createSampleZone(2L);

        ZoneDTO dto1 = createSampleZoneDTO(1L);
        ZoneDTO dto2 = createSampleZoneDTO(2L);

        when(zoneRepository.findByOwnerId(owner.getId())).thenReturn(List.of(zone1, zone2));
        when(zoneMapper.toDto(zone1)).thenReturn(dto1);
        when(zoneMapper.toDto(zone2)).thenReturn(dto2);

        List<ZoneDTO> result =  zoneService.getZonesByOwner(owner.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(dto1.getId(), result.get(0).getId());
        assertEquals(dto2.getId(), result.get(1).getId());

        verify(zoneRepository).findByOwnerId(owner.getId());
        verify(zoneMapper).toDto(zone1);
        verify(zoneMapper).toDto(zone2);
    }
}