package com.ares.ares_server.service;

import com.ares.ares_server.config.MailchimpConfig;
import com.ares.ares_server.dto.MissionEmailDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MailchimpEmailService implements EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(MailchimpEmailService.class);
    private static final String MAILCHIMP_API_URL = "https://mandrillapp.com/api/1.0/messages/send-template";
    
    private final MailchimpConfig mailchimpConfig;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public MailchimpEmailService(MailchimpConfig mailchimpConfig) {
        this.mailchimpConfig = mailchimpConfig;
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public boolean sendMissionEmail(MissionEmailDTO emailData) throws Exception {
        try {
            ObjectNode payload = buildMailchimpPayload(emailData);
            
            RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json")
            );
            
            Request request = new Request.Builder()
                .url(MAILCHIMP_API_URL)
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    logger.info("Mission email sent successfully to: {}", emailData.getPlayerEmail());
                    return true;
                } else {
                    logger.error("Failed to send email. Status: {}, Body: {}", 
                        response.code(), 
                        response.body() != null ? response.body().string() : "empty");
                    return false;
                }
            }
        } catch (IOException e) {
            logger.error("Error sending mission email to {}: {}", emailData.getPlayerEmail(), e.getMessage());
            throw new Exception("Failed to send mission email", e);
        }
    }
    
    private ObjectNode buildMailchimpPayload(MissionEmailDTO emailData) {
        ObjectNode payload = objectMapper.createObjectNode();
        
        payload.put("key", mailchimpConfig.getApiKey());
        payload.put("template_name", mailchimpConfig.getTemplateName());
        payload.set("template_content", objectMapper.createArrayNode());
        
        ObjectNode message = objectMapper.createObjectNode();
        
        ArrayNode to = objectMapper.createArrayNode();
        ObjectNode recipient = objectMapper.createObjectNode();
        recipient.put("email", emailData.getPlayerEmail());
        recipient.put("name", emailData.getPlayerName());
        recipient.put("type", "to");
        to.add(recipient);
        message.set("to", to);
        
        message.put("from_email", mailchimpConfig.getFromEmail());
        message.put("from_name", mailchimpConfig.getFromName());
        message.put("subject", "Your Personalized Missions");
        
        ArrayNode globalMergeVars = objectMapper.createArrayNode();
        
        ObjectNode playerNameVar = objectMapper.createObjectNode();
        playerNameVar.put("name", "PLAYER_NAME");
        playerNameVar.put("content", emailData.getPlayerName());
        globalMergeVars.add(playerNameVar);
        
        ObjectNode missionsVar = objectMapper.createObjectNode();
        missionsVar.put("name", "MISSIONS_DESC");
        missionsVar.put("content", emailData.getMissionsDescription());
        globalMergeVars.add(missionsVar);
        
        message.set("global_merge_vars", globalMergeVars);
        message.put("merge_language", "mailchimp");
        payload.set("message", message);
        
        return payload;
    }
}
