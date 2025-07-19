package com.talentradar.talentradarnotificationservicerw.services.impl;

import com.talentradar.talentradarnotificationservicerw.services.EmailService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceDevImpl implements EmailService {

    @Override
    public void sendEmail(String to, String subject, String body) {
        System.out.println("📧 [MOCK] Email Sent");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }
}
