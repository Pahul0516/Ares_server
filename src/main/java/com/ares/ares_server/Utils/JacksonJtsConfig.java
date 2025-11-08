package com.ares.ares_server.Utils;

import com.fasterxml.jackson.databind.Module;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonJtsConfig {
    @Bean
    public Module jtsModule() {
        return new JtsModule();
    }
}

