package com.talentradar.talentradarnotificationservicerw.config;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {
    @KafkaListener(topics = "feedback.submitted", groupId = "feedback-service")
    public void feedbackListener(String message) {
        // construct notification
        // save notification
        // publish the notification via the websocket / send the notification via email
    }

    @KafkaListener(topics = "assessment.submitted", groupId = "feedback-service")
    public void assessmentListener(String message) {
        // construct notification
        // save notification
        // publish the notification via the websocket / send the notification via email
    }

}
