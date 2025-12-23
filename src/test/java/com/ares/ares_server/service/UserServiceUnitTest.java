package com.ares.ares_server.service;

import com.ares.ares_server.domain.Run;
import com.ares.ares_server.domain.User;
import com.ares.ares_server.domain.Zone;
import com.ares.ares_server.dto.RunnerDTO;
import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.dto.UserStatsDTO;
import com.ares.ares_server.dto.mappers.UserMapper;
import com.ares.ares_server.exceptions.UserDoesNotExistsException;
import com.ares.ares_server.repository.RunRepository;
import com.ares.ares_server.repository.UserRepository;
import com.ares.ares_server.repository.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RunRepository runRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void tearDown() {
        clearInvocations(userRepository, userMapper);
    }

    @Test
    void getAllUsers_returns_list() {
        User u1 = new User(); u1.setId(UUID.randomUUID()); u1.setUsername("a");
        User u2 = new User(); u2.setId(UUID.randomUUID()); u2.setUsername("b");

        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));
        when(userMapper.toDto(u1)).thenReturn(new UserDTO(null, u1.getUsername(), null, null));
        when(userMapper.toDto(u2)).thenReturn(new UserDTO(null, u2.getUsername(), null, null));

        List<UserDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("a", result.get(0).getUsername());
        assertEquals("b", result.get(1).getUsername());

        verify(userRepository).findAll();
        verify(userMapper).toDto(u1);
        verify(userMapper).toDto(u2);
    }

    @Test
    void getUserByEmail_found() {
        UUID id = UUID.randomUUID();
        String email = "test@test.com";
        User u = new User(); u.setId(id); u.setUsername("five"); u.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(u));
        when(userMapper.toDto(u)).thenReturn(new UserDTO(null, "five", "test@test.com", null));

        UserDTO user = userService.getUserByEmail(email);
        assertNotNull(user);
        assertEquals("five", user.getUsername());
        assertEquals("test@test.com", user.getEmail());

        verify(userMapper).toDto(u);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getUserByEmail_notFound() {
        String email = "wrong_email";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistsException.class, () -> userService.getUserByEmail(email));

        verify(userRepository).findByEmail(email);
    }

    @Test
    void updateUser_success() {
        UUID id = UUID.randomUUID();
        User existing = new User(); existing.setId(id); existing.setEmail("old@x.com"); existing.setUsername("old");
        UserDTO updatedDTO = new UserDTO(null, "new", "new@x.com", null);

        when(userRepository.findByEmail(existing.getEmail())).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toDto(existing)).thenReturn(updatedDTO);

        UserDTO result = userService.updateUser(existing.getEmail(), updatedDTO);
        assertNotNull(result);
        assertEquals("new", result.getUsername());
        assertEquals("new@x.com", result.getEmail());

        verify(userRepository).findByEmail("old@x.com");
        verify(userRepository).save(existing);
        verify(userMapper).toDto(existing);
    }

    @Test
    void updateUser_notFound() {
        String email = "worng_email";
        UserDTO updateDto = new UserDTO(null, "new", "new@x.com", null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistsException.class, () -> userService.updateUser(email, updateDto));

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_success() {
        String email = "new@x.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);
        doNothing().when(userRepository).deleteByEmail(email);

        userService.deleteUser(email);

        verify(userRepository).existsByEmail(email);
        verify(userRepository).deleteByEmail(email);
    }

    @Test
    void deleteUser_notFound() {
        String email = "new@x.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        assertThrows(UserDoesNotExistsException.class, () -> userService.deleteUser(email));

        verify(userRepository).existsByEmail(email);
    }

    @Test
    void getUserStats_success() {
        UUID userId = UUID.randomUUID();
        String email = "stats@test.com";

        User user = new User();
        user.setId(userId);
        user.setEmail(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(userMapper.toDto(user))
                .thenReturn(new UserDTO(userId, null, email, null));

        Run r1 = new Run();
        r1.setDistance(100F);
        r1.setDuration(60);

        Run r2 = new Run();
        r2.setDistance(200F);
        r2.setDuration(120);

        when(runRepository.findByOwnerId(userId))
                .thenReturn(Arrays.asList(r1, r2));

        Zone z1 = new Zone();
        z1.setArea(10.5);

        Zone z2 = new Zone();
        z2.setArea(20.0);

        when(zoneRepository.findByOwnerId(userId))
                .thenReturn(Arrays.asList(z1, z2));

        UserStatsDTO stats = userService.getUserStats(email);

        assertNotNull(stats);

        assertEquals(300, stats.getTotalDistance());   // 100 + 200
        assertEquals(180, stats.getTimeRunning());     // 60 + 120
        assertEquals(30.5f, stats.getTotalArea());     // 10.5 + 20.0

        verify(userRepository).findByEmail(email);
        verify(runRepository).findByOwnerId(userId);
        verify(zoneRepository).findByOwnerId(userId);
    }

    @Test
    void getTopTenRunners_returns_sorted_top_10() {
        // GIVEN
        UUID u1Id = UUID.randomUUID();
        UUID u2Id = UUID.randomUUID();
        UUID u3Id = UUID.randomUUID();

        UserDTO u1 = new UserDTO(u1Id, "alice", null, null);
        UserDTO u2 = new UserDTO(u2Id, "bob", null, null);
        UserDTO u3 = new UserDTO(u3Id, "charlie", null, null);

        // spy because getTopTenRunners() calls getAllUsers()
        UserService spyService = spy(userService);
        doReturn(List.of(u1, u2, u3)).when(spyService).getAllUsers();

        Zone z1a = new Zone(); z1a.setArea(10.0);
        Zone z1b = new Zone(); z1b.setArea(20.0); // total = 30

        Zone z2a = new Zone(); z2a.setArea(50.0); // total = 50

        Zone z3a = new Zone(); z3a.setArea(5.0);  // total = 5

        when(zoneRepository.findByOwnerId(u1Id)).thenReturn(List.of(z1a, z1b));
        when(zoneRepository.findByOwnerId(u2Id)).thenReturn(List.of(z2a));
        when(zoneRepository.findByOwnerId(u3Id)).thenReturn(List.of(z3a));

        // WHEN
        List<RunnerDTO> result = spyService.getTopTenRunners();

        // THEN
        assertEquals(3, result.size());

        // sorted DESC by score
        assertEquals("bob", result.get(0).getUsername());
        assertEquals(50.0, result.get(0).getScore());

        assertEquals("alice", result.get(1).getUsername());
        assertEquals(30.0, result.get(1).getScore());

        assertEquals("charlie", result.get(2).getUsername());
        assertEquals(5.0, result.get(2).getScore());

        verify(zoneRepository).findByOwnerId(u1Id);
        verify(zoneRepository).findByOwnerId(u2Id);
        verify(zoneRepository).findByOwnerId(u3Id);
    }

}