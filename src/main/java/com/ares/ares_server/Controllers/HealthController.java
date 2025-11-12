package com.ares.ares_server.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping("/check")
    public ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("Healthy");
    }

    @RequestMapping(value = "/check", method = RequestMethod.HEAD)
    public ResponseEntity<Void> headCheck() {
        return ResponseEntity.ok().build();
    }
}
