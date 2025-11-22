package com.ares.ares_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class AresServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AresServerApplication.class, args);
    }

}
