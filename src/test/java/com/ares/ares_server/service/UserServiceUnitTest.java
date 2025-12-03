package com.ares.ares_server.service;

import com.ares.ares_server.domain.User;
import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.dto.mappers.UserMapper;
import com.ares.ares_server.exceptios.UserDoesNotExistsException;
import com.ares.ares_server.repository.UserRepository;
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

        assertThrows(UserDoesNotExistsException.class, () -> {
            userService.getUserByEmail(email);
        });

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

        assertThrows(UserDoesNotExistsException.class, () -> {
            userService.updateUser(email, updateDto);
        });

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

        assertThrows(UserDoesNotExistsException.class, () -> {
            userService.deleteUser(email);
        });

        verify(userRepository).existsByEmail(email);
    }
}