package com.ares.ares_server.dto.mappers;

import com.ares.ares_server.dto.UserDTO;
import com.ares.ares_server.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto(User user);
    User fromDto(UserDTO userDTO);
}