package com.ares.ares_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionEmailDTO {
    private String playerName;
    private String playerEmail;
    private String missionsDescription;
}
