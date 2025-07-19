package com.talentradar.talentradarnotificationservicerw.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentradar.talentradarnotificationservicerw.domain.entities.Notification;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationEventType;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationType;
import com.talentradar.talentradarnotificationservicerw.domain.events.EventLog;
import com.talentradar.talentradarnotificationservicerw.domain.events.FeedBackServiceEvent;
import com.talentradar.talentradarnotificationservicerw.services.EmailService;
import com.talentradar.talentradarnotificationservicerw.services.NotificationServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {
    private final ObjectMapper objectMapper;
    private final NotificationServices notificationServices;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    private enum kafkaTopic {
        assessment_submitted, feedback_submitted
    }

    @KafkaListener(topics = "feedback.submitted", groupId = "feedback-service")
    public void feedbackListener(String message) {
        try {
            FeedBackServiceEvent event = objectMapper.readValue(message, FeedBackServiceEvent.class);
            Notification notification = Notification.builder()
                    .category(event.category())
                    .content(event.content())
                    .recipientId(event.recipientId())
                    .title(event.title())
                    .type(event.type())
                    .eventType(NotificationEventType.FEEDBACK)
                    .build();

            Notification savedNotification = notificationServices.saveNotification(notification);
            handleUpdates(savedNotification, event, kafkaTopic.feedback_submitted);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @KafkaListener(topics = "assessment.submitted", groupId = "feedback-service")
    public void assessmentListener(String message) {
        try {
            FeedBackServiceEvent event = objectMapper.readValue(message, FeedBackServiceEvent.class);
            Notification notification = Notification.builder()
                    .category(event.category())
                    .content(event.content())
                    .recipientId(event.recipientId())
                    .title(event.title())
                    .type(event.type())
                    .eventType(NotificationEventType.ASSESSMENT)
                    .build();

            Notification savedNotification = notificationServices.saveNotification(notification);
            handleUpdates(savedNotification, event, kafkaTopic.assessment_submitted);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void sendWebSocketPush(Notification notification, FeedBackServiceEvent event) {
        messagingTemplate.convertAndSendToUser(
                event.recipientId(),
                "/queue/notifications",
                notification
        );
    }

    private void handleUpdates(Notification notification, FeedBackServiceEvent event, kafkaTopic topic) {
        if (event.type().equals(NotificationType.EMAIL)) {
            emailService.sendEmail(event.recipientEmail(), event.title(), event.content());
            logActivity(topic, event);
        } else {
            sendWebSocketPush(notification, event);
            logActivity(topic, event);
        }
    }

    private void logActivity(kafkaTopic topic, FeedBackServiceEvent event) {
        log.info(
                EventLog.builder()
                        .trigger(
                                topic.equals(kafkaTopic.feedback_submitted) ?
                                        "Event triggered by a new feedback submitted" :
                                        "Event triggered by a new assessment submitted"
                        )
                        .event(event)
                        .build()
                        .toString()
        );
    }
}
