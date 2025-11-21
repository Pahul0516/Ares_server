package com.ares.ares_server.Controllers;

import com.ares.ares_server.DTOs.Mappers.UserMapper;
import com.ares.ares_server.DTOs.UserDTO;
import com.ares.ares_server.Domain.User;
import com.ares.ares_server.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController using Mockito and standalone MockMvc.
 * Uses UUID-based IDs.
 */
class UserControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @AfterEach
    void tearDown() {
        clearInvocations(userRepository, userMapper);
    }

    @Test
    void getAllUsers_returns_list() throws Exception {
        User u1 = new User(); u1.setId(UUID.randomUUID()); u1.setUsername("a");
        User u2 = new User(); u2.setId(UUID.randomUUID()); u2.setUsername("b");

        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));
        when(userMapper.toDto(u1)).thenReturn(new UserDTO(u1.getId(), u1.getUsername(), null, null));
        when(userMapper.toDto(u2)).thenReturn(new UserDTO(u2.getId(), u2.getUsername(), null, null));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value(u1.getUsername()))
                .andExpect(jsonPath("$[1].username").value(u2.getUsername()))
                .andExpect(jsonPath("$[0].email").value(u1.getEmail()))
                .andExpect(jsonPath("$[1].email").value(u2.getEmail()))
                .andExpect(jsonPath("$[0].encryptedPassword").value(u1.getEncryptedPassword()))
                .andExpect(jsonPath("$[1].encryptedPassword").value(u2.getEncryptedPassword()));

        verify(userRepository).findAll();
    }

    @Test
    void getUserById_found() throws Exception {
        UUID id = UUID.randomUUID();
        User u = new User(); u.setId(id); u.setUsername("five");

        when(userRepository.findById(id)).thenReturn(Optional.of(u));
        when(userMapper.toDto(u)).thenReturn(new UserDTO(id, "five", null, null));

        mockMvc.perform(get("/api/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("five"));

        verify(userRepository).findById(id);
    }

    @Test
    void getUserById_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/" + id))
                .andExpect(status().isNotFound());

        verify(userRepository).findById(id);
    }

    @Test
    void updateUser_success() throws Exception {
        UUID id = UUID.randomUUID();
        User existing = new User(); existing.setId(id); existing.setEmail("old@x.com"); existing.setUsername("old");
        UserDTO updatedDTO = new UserDTO(null, "new", "new@x.com", null);


        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toDto(existing)).thenReturn(new UserDTO(id, "new", "new@x.com", null));

        mockMvc.perform(put("/api/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("new@x.com"))
                .andExpect(jsonPath("$.username").value("new"));

        verify(userRepository).findById(id);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        UserDTO updateDto = new UserDTO(null, "new", "new@x.com", null);

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(userRepository).findById(id);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_success() throws Exception {
        UUID id1 = UUID.randomUUID();
        when(userRepository.existsById(id1)).thenReturn(true);
        doNothing().when(userRepository).deleteById(id1);

        mockMvc.perform(delete("/api/users/" + id1))
                .andExpect(status().isNoContent());

        verify(userRepository).existsById(id1);
        verify(userRepository).deleteById(id1);
    }

    @Test
    void deleteUser_notFound() throws Exception {
        UUID id2 = UUID.randomUUID();
        when(userRepository.existsById(id2)).thenReturn(false);

        mockMvc.perform(delete("/api/users/" + id2))
                .andExpect(status().isNotFound());

        verify(userRepository).existsById(id2);
    }
}