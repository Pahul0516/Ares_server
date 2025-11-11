package com.ares.ares_server.Task;

import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
public class KeepAliveTask {
    //TODO : change url to server url
    private static final String HEALTH_URL = "http://localhost:9001/api/health/check";
    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedRate = 10000)
    public void keepAlive() {
        try {
            RequestEntity<Void> request = new RequestEntity<>(HttpMethod.HEAD, URI.create(HEALTH_URL));
            ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("[KeepAliveTask] Server is active (status " + response.getStatusCode() + ")");
            } else {
                System.out.println("[KeepAliveTask] Unexpected status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("[KeepAliveTask] Failed to ping server: " + e.getMessage());
        }
    }
}