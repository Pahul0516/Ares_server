package com.ares.ares_server.service;

import com.ares.ares_server.domain.User;
import com.ares.ares_server.dto.AuthDTO;
import com.ares.ares_server.dto.CredentialsDTO;
import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.dto.mappers.UserMapper;
import com.ares.ares_server.exceptions.InvalidCredentialsException;
import com.ares.ares_server.exceptions.UserAlreadyExistsException;
import com.ares.ares_server.exceptions.UserDoesNotExistsException;
import com.ares.ares_server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtSecret", "mySecretKeymySecretKeymySecretKeymySecretKey"); // must be 32+ chars
        ReflectionTestUtils.setField(authService, "jwtExpirationMs", 3600000L); // 1 hour
    }

    @Test
    void signUp_success() {
        UserDTO userDTO = new UserDTO(null ,"john", "john@test.com", "pass");
        User user = new User();
        user.setEmail(userDTO.getEmail());

        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(false);
        when(userMapper.fromDto(userDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        AuthDTO result = authService.signUp(userDTO);

        assertNotNull(result.getToken());
        verify(userRepository).existsByEmail(userDTO.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void signUp_conflict_userAlreadyExists() {
        UserDTO userDTO = new UserDTO(null, "john", "john@test.com", "pass");

        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.signUp(userDTO));
        verify(userRepository).existsByEmail(userDTO.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        CredentialsDTO credentials = new CredentialsDTO("john@test.com", "secret");
        User user = new User();
        user.setEmail(credentials.getEmail());
        user.setEncryptedPassword("secret");

        when(userRepository.findByEmail(credentials.getEmail())).thenReturn(Optional.of(user));

        String token = authService.login(credentials);

        assertNotNull(token);
        verify(userRepository).findByEmail(credentials.getEmail());
    }

    @Test
    void login_invalidCredentials() {
        CredentialsDTO credentials = new CredentialsDTO("john@test.com", "wrong");
        User user = new User();
        user.setEmail(credentials.getEmail());
        user.setEncryptedPassword("secret");

        when(userRepository.findByEmail(credentials.getEmail())).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(credentials));
        verify(userRepository).findByEmail(credentials.getEmail());
    }

    @Test
    void login_userNotFound() {
        CredentialsDTO credentials = new CredentialsDTO("missing@test.com", "secret");

        when(userRepository.findByEmail(credentials.getEmail())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(credentials));
        verify(userRepository).findByEmail(credentials.getEmail());
    }

    // --- CHANGE PASSWORD ---
    @Test
    void changePassword_success() {
        String email = "john@test.com";
        String newPassword = "newSecret";
        User user = new User();
        user.setEmail(email);
        user.setEncryptedPassword("old");

        when(userRepository.existsByEmail(email)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        authService.changePassword(email, newPassword);

        assertEquals("newSecret", user.getEncryptedPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_userNotFound() {
        String email = "missing@test.com";

        when(userRepository.existsByEmail(email)).thenReturn(false);

        assertThrows(UserDoesNotExistsException.class, () -> authService.changePassword(email, "newSecret"));
        verify(userRepository, never()).save(any());
    }

    // --- REFRESH ---
    @Test
    void refresh_success() {
        CredentialsDTO credentials = new CredentialsDTO("john@test.com", "secret");
        User user = new User();
        user.setEmail(credentials.getEmail());
        user.setEncryptedPassword("secret");

        when(userRepository.findByEmail(credentials.getEmail())).thenReturn(Optional.of(user));

        String token = authService.refresh(credentials);

        assertNotNull(token);
        verify(userRepository).findByEmail(credentials.getEmail());
    }
}
