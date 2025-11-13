package com.ares.ares_server.Controllers;

import com.ares.ares_server.Domain.User;
import com.ares.ares_server.Repository.UserRepositroy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
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
    private UserRepositroy userRepository;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userController, "userRepository", userRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @AfterEach
    void tearDown() {
        clearInvocations(userRepository);
    }

    @Test
    void signup_success() throws Exception {
        User input = new User();
        input.setEmail("new@example.com");
        input.setUsername("newuser");
        input.setEncryptedPassword("pwd");

        User saved = new User();
        UUID id = UUID.randomUUID();
        saved.setId(id);
        saved.setEmail(input.getEmail());
        saved.setUsername(input.getUsername());
        saved.setEncryptedPassword(input.getEncryptedPassword());

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("new@example.com"));

        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_emailAlreadyExists_badRequest() throws Exception {
        User input = new User();
        input.setEmail("exists@example.com");

        when(userRepository.existsByEmail("exists@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(userRepository).existsByEmail("exists@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() throws Exception {
        User login = new User();
        login.setUsername("john");
        login.setEncryptedPassword("secret");

        UUID id = UUID.randomUUID();
        User stored = new User();
        stored.setId(id);
        stored.setUsername("john");
        stored.setEncryptedPassword("secret");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(stored));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("john"));

        verify(userRepository).findByUsername("john");
    }

    @Test
    void login_invalidCredentials_unauthorized() throws Exception {
        User login = new User();
        login.setUsername("john");
        login.setEncryptedPassword("bad");

        User stored = new User();
        stored.setUsername("john");
        stored.setEncryptedPassword("secret");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(stored));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());

        verify(userRepository).findByUsername("john");
    }

    @Test
    void logout_and_refresh_endpoints_return_ok() throws Exception {
        mockMvc.perform(post("/api/users/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("User logged out"));

        mockMvc.perform(post("/api/users/refresh"))
                .andExpect(status().isOk())
                .andExpect(content().string("Token refreshed successfully"));
    }

    @Test
    void getAllUsers_returns_list() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        User u1 = new User(); u1.setId(id1); u1.setUsername("a");
        User u2 = new User(); u2.setId(id2); u2.setUsername("b");
        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(id1.toString()))
                .andExpect(jsonPath("$[1].id").value(id2.toString()));

        verify(userRepository).findAll();
    }

    @Test
    void getUserById_found() throws Exception {
        UUID id = UUID.randomUUID();
        User u = new User(); u.setId(id); u.setUsername("five");
        when(userRepository.findById(id)).thenReturn(Optional.of(u));

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
        User updated = new User(); updated.setEmail("new@x.com"); updated.setUsername("new");

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User arg = invocation.getArgument(0);
            arg.setId(id);
            return arg;
        });

        mockMvc.perform(put("/api/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
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
        User updated = new User(); updated.setEmail("no@x.com");

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound());

        verify(userRepository).findById(id);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_success_and_notFound() throws Exception {
        UUID id1 = UUID.randomUUID();
        when(userRepository.existsById(id1)).thenReturn(true);
        doNothing().when(userRepository).deleteById(id1);

        mockMvc.perform(delete("/api/users/" + id1))
                .andExpect(status().isNoContent());

        verify(userRepository).existsById(id1);
        verify(userRepository).deleteById(id1);

        UUID id2 = UUID.randomUUID();
        when(userRepository.existsById(id2)).thenReturn(false);

        mockMvc.perform(delete("/api/users/" + id2))
                .andExpect(status().isNotFound());

        verify(userRepository).existsById(id2);
    }

    @Test
    void forgot_and_reset_password_endpoints() throws Exception {
        mockMvc.perform(post("/api/users/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"email@example.com\""))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset link sent to email@example.com"));

        mockMvc.perform(post("/api/users/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"newpass\""))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset successfully"));
    }

    @Test
    void changePassword_success_and_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        User existing = new User(); existing.setId(id); existing.setEncryptedPassword("oldpwd");
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/api/users/" + id + "/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"newpwd\""))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully"));

        verify(userRepository).findById(id);
        verify(userRepository).save(any(User.class));

        UUID missingId = UUID.randomUUID();
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/users/" + missingId + "/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"x\""))
                .andExpect(status().isNotFound());

        verify(userRepository).findById(missingId);
    }
}
