package com.talentradar.talentradarnotificationservicerw.services;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
