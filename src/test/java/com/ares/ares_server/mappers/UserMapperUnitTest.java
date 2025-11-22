package com.ares.ares_server.mappers;

import com.ares.ares_server.dto.mappers.UserMapper;
import com.ares.ares_server.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserMapperUnitTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void toDtoTest() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@x.com");
        user.setEncryptedPassword("encryptedPassword123");

        var userDTO = userMapper.toDto(user);

        assertNotNull(userDTO);
        assertEquals(user.getId(), userDTO.getId());
        assertEquals(user.getUsername(), userDTO.getUsername());
        assertEquals(user.getEmail(), userDTO.getEmail());
        assertEquals(user.getEncryptedPassword(), userDTO.getEncryptedPassword());
    }

    @Test
    void toDtoTest_null() {
        User user = null;

        var userDTO = userMapper.toDto(user);

        assertNull(userDTO);
    }

    @Test
    void fromDtoTest() {
        var userDTO = com.ares.ares_server.dto.UserDTO.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@x.com")
                .encryptedPassword("encryptedPassword123")
                .build();

        var user = userMapper.fromDto(userDTO);

        assertNotNull(user);
        assertEquals(userDTO.getId(), user.getId());
        assertEquals(userDTO.getUsername(), user.getUsername());
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getEncryptedPassword(), user.getEncryptedPassword());
    }

    @Test
    void fromDtoTest_null() {
        var userDTO = (com.ares.ares_server.dto.UserDTO) null;
        var user = userMapper.fromDto(userDTO);
        assertNull(user);
    }
}