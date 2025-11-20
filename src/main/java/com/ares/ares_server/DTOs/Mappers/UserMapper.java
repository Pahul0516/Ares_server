package com.ares.ares_server.DTOs.Mappers;

import com.ares.ares_server.DTOs.UserDTO;
import com.ares.ares_server.Domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto(User user);
    User fromDto(UserDTO userDTO);
}