package com.ares.ares_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CredentialsDTO {
    private String email;
    private String password;
}
