package com.ares.ares_server.Mappers;

import com.ares.ares_server.DTOs.Mappers.UserMapper;
import com.ares.ares_server.Domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
public class UserMapperUnitTest {

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

        assert userDTO != null;
        assert userDTO.getId().equals(user.getId());
        assert userDTO.getUsername().equals(user.getUsername());
        assert userDTO.getEmail().equals(user.getEmail());
        assert userDTO.getEncryptedPassword().equals(user.getEncryptedPassword());
    }

    @Test
    void toDtoTest_null() {
        User user = null;

        var userDTO = userMapper.toDto(user);

        assertNull(userDTO);
    }

    @Test
    void fromDtoTest() {
        var userDTO = com.ares.ares_server.DTOs.UserDTO.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@x.com")
                .encryptedPassword("encryptedPassword123")
                .build();
        var user = userMapper.fromDto(userDTO);
        assert user != null;
        assert user.getId().equals(userDTO.getId());
        assert user.getUsername().equals(userDTO.getUsername());
        assert user.getEmail().equals(userDTO.getEmail());
        assert user.getEncryptedPassword().equals(userDTO.getEncryptedPassword());
    }

    @Test
    void fromDtoTest_null() {
        var userDTO = (com.ares.ares_server.DTOs.UserDTO) null;
        var user = userMapper.fromDto(userDTO);
        assertNull(user);
    }
}