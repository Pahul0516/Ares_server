package com.ares.ares_server.controllers;

import com.ares.ares_server.dto.RunnerDTO;
import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.dto.UserStatsDTO;
import com.ares.ares_server.exceptions.UserDoesNotExistsException;
import com.ares.ares_server.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.MediaType;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static com.ares.ares_server.dto.mappers.RunMapper.objectMapper;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler()) // register your advice
                .build();
    }

    @AfterEach
    void tearDown() {
        clearInvocations(userService);
    }

    @Test
    void getAllUsers_returns_list() throws Exception {
        UserDTO userDto1 = new UserDTO(null, "a", "a@test.com", "encryptedPassword1");
        UserDTO userDto2 = new UserDTO(null, "b", "b@test.com", "encryptedPassword2");
        List<UserDTO> userDTOList = Arrays.asList(userDto1, userDto2);

        when(userService.getAllUsers()).thenReturn(userDTOList);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value(userDto1.getUsername()))
                .andExpect(jsonPath("$[1].username").value(userDto2.getUsername()))
                .andExpect(jsonPath("$[0].email").value(userDto1.getEmail()))
                .andExpect(jsonPath("$[1].email").value(userDto2.getEmail()))
                .andExpect(jsonPath("$[0].encryptedPassword").value(userDto1.getEncryptedPassword()))
                .andExpect(jsonPath("$[1].encryptedPassword").value(userDto2.getEncryptedPassword()));

        verify(userService).getAllUsers();
    }

    @Test
    void getUserByEmail_found() throws Exception {
        String email = "test@test.com";
        UserDTO userDTO = new UserDTO(null, "five", email, null);

        when(userService.getUserByEmail(email)).thenReturn(userDTO);

        mockMvc.perform(get("/api/users/" + email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.username").value("five"));

        verify(userService).getUserByEmail(email);
    }

    @Test
    void getUserByEmail_notFound() throws Exception {
        String email = "wrong_email";
        String expectedMessage = "User with email " + email + " does not exist";

        when(userService.getUserByEmail(email))
                .thenThrow(new UserDoesNotExistsException(expectedMessage));

        mockMvc.perform(get("/api/users/" + email))
                .andExpect(status().isNotFound())
                .andExpect(content().string(expectedMessage));

        verify(userService).getUserByEmail(email);
    }

    @Test
    void updateUser_success() throws Exception {
        String email = "old_email";
        UserDTO updatedDTO = new UserDTO(null, "new", "new@x.com", null);

        when(userService.updateUser(email, updatedDTO)).thenReturn(updatedDTO);

        mockMvc.perform(put("/api/users/" + email)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@x.com"))
                .andExpect(jsonPath("$.username").value("new"));

        verify(userService).updateUser(email, updatedDTO);
    }

    @Test
    void updateUser_notFound() throws Exception {
        String email = "wrong_email";
        UserDTO updateDto = new UserDTO(null, "new", "new@x.com", null);
        String expectedMessage = "User with email " + email + " does not exist";

        when(userService.updateUser(email, updateDto))
                .thenThrow(new UserDoesNotExistsException(expectedMessage));

        mockMvc.perform(put("/api/users/" + email)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(expectedMessage));

        verify(userService).updateUser(email, updateDto);
    }

    @Test
    void deleteUser_success() throws Exception {
        String email = "new@x.com";

        doNothing().when(userService).deleteUser(email);

        mockMvc.perform(delete("/api/users/" + email))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(email);
    }

    @Test
    void deleteUser_notFound() throws Exception {
        String email = "new@x.com";
        String expectedMessage = "User with email " + email + " does not exist";

        doThrow(new UserDoesNotExistsException(expectedMessage))
                .when(userService).deleteUser(email);

        mockMvc.perform(delete("/api/users/" + email))
                .andExpect(status().isNotFound())
                .andExpect(content().string(expectedMessage));

        verify(userService).deleteUser(email);
    }

    @Test
    void getUserStats_success() throws Exception {
        String email = "stats@test.com";

        UserStatsDTO statsDTO = new UserStatsDTO();
        statsDTO.setTotalDistance(300);
        statsDTO.setTimeRunning(180);
        statsDTO.setTotalArea(30.5);

        when(userService.getUserStats(email)).thenReturn(statsDTO);

        mockMvc.perform(get("/api/users/{email}/stats", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDistance").value(300))
                .andExpect(jsonPath("$.timeRunning").value(180))
                .andExpect(jsonPath("$.totalArea").value(30.5));

        verify(userService).getUserStats(email);
    }

    @Test
    void getUserStats_userNotFound() throws Exception {
        String email = "missing@test.com";
        String expectedMessage = "User with email " + email + " does not exist";

        when(userService.getUserStats(email))
                .thenThrow(new UserDoesNotExistsException(expectedMessage));

        mockMvc.perform(get("/api/users/{email}/stats", email))
                .andExpect(status().isNotFound())
                .andExpect(content().string(expectedMessage));

        verify(userService).getUserStats(email);
    }

    @Test
    void updateLeaderboard_returnsTopTenRunners() {
        RunnerDTO run1 = new RunnerDTO();
        run1.setUsername("Alice");
        run1.setScore(120.0);

        RunnerDTO run2 = new RunnerDTO();
        run2.setUsername("Bob");
        run2.setScore(110.0);

        List<RunnerDTO> runners = List.of(run1, run2);

        when(userService.getTopTenRunners()).thenReturn(runners);

        List<RunnerDTO> result = userController.updateLeaderboard();

        Assertions.assertEquals(runners, result);
        verify(userService).getTopTenRunners();
    }

}