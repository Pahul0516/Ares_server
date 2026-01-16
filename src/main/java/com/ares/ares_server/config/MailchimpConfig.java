package com.ares.ares_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class MailchimpConfig {
    
    @Value("${mailchimp.api.key}")
    private String apiKey;
    
    @Value("${mailchimp.template.name}")
    private String templateName;
    
    @Value("${mailchimp.from.email}")
    private String fromEmail;
    
    @Value("${mailchimp.from.name}")
    private String fromName;
}
