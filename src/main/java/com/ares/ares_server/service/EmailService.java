package com.ares.ares_server.service;

import com.ares.ares_server.dto.MissionEmailDTO;

public interface EmailService {
    /**
     * Send a mission email to a player using Mailchimp transactional email
     * @param emailData The email data containing player name, email, and missions
     * @return true if email was sent successfully, false otherwise
     */
    boolean sendMissionEmail(MissionEmailDTO emailData) throws Exception;
}
