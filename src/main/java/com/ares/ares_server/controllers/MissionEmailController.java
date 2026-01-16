package com.ares.ares_server.controllers;

import com.ares.ares_server.AI.AIMissionGenerator;
import com.ares.ares_server.domain.User;
import com.ares.ares_server.dto.MissionEmailDTO;
import com.ares.ares_server.service.EmailService;
import com.ares.ares_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin
public class MissionEmailController {
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Send personalized missions email to a specific user
     * @param userId The UUID of the user
     * @return Success or error message
     */
    @PostMapping("/send/{userId}")
    public ResponseEntity<?> sendMissionEmail(@PathVariable UUID userId) {
        try {
            User user = userService.getUserEntityById(userId);
            
            Object missionsData = AIMissionGenerator.runPythonMissionGenerator();
            String missionsDescription = missionsData.toString();
            
            MissionEmailDTO emailData = new MissionEmailDTO(
                user.getUsername(),
                user.getEmail(),
                missionsDescription
            );
            
            boolean sent = emailService.sendMissionEmail(emailData);
            
            if (sent) {
                return ResponseEntity.ok("Mission email sent successfully to " + user.getEmail());
            } else {
                return ResponseEntity.status(500).body("Failed to send mission email");
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Send personalized missions email to all users
     * @return Summary of email sending results
     */
    @PostMapping("/send-all")
    public ResponseEntity<?> sendMissionEmailToAll() {
        try {
            var users = userService.getAllUserEntities();
            int successCount = 0;
            int failureCount = 0;
            
            for (User user : users) {
                try {
                    Object missionsData = AIMissionGenerator.runPythonMissionGenerator();
                    String missionsDescription = missionsData.toString();
                    
                    MissionEmailDTO emailData = new MissionEmailDTO(
                        user.getUsername(),
                        user.getEmail(),
                        missionsDescription
                    );
                    
                    boolean sent = emailService.sendMissionEmail(emailData);
                    if (sent) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                    
                } catch (Exception e) {
                    failureCount++;
                    System.err.println("Failed to send email to " + user.getEmail() + ": " + e.getMessage());
                }
            }
            
            return ResponseEntity.ok(String.format(
                "Emails sent: %d successful, %d failed out of %d total users",
                successCount, failureCount, users.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Test endpoint to send a test email with custom missions text
     * @param emailData The email data to send
     * @return Success or error message
     */
    @PostMapping("/test")
    public ResponseEntity<?> sendTestEmail(@RequestBody MissionEmailDTO emailData) {
        try {
            boolean sent = emailService.sendMissionEmail(emailData);
            
            if (sent) {
                return ResponseEntity.ok("Test email sent successfully");
            } else {
                return ResponseEntity.status(500).body("Failed to send test email");
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
