package com.ares.ares_server.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class HealthControllerUnitTest {

    private MockMvc mockMvc;

    @InjectMocks
    private HealthController healthController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(healthController).build();
    }

    @Test
    void headCheck_returnsOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.head("/api/health/check"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}