package com.talentradar.talentradarnotificationservicerw.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.talentradar.talentradarnotificationservicerw.domain.entities.Notification;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationCategory;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationEventType;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationType;
import com.talentradar.talentradarnotificationservicerw.domain.events.AssessmentEvent;
import com.talentradar.talentradarnotificationservicerw.domain.events.FeedbackEvent;
import com.talentradar.talentradarnotificationservicerw.services.EmailService;
import com.talentradar.talentradarnotificationservicerw.services.NotificationServices;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {
    private final NotificationServices notificationServices;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.ASSESSMENT_EVENTS_QUEUE)
    @Transactional
    public void handleAssessmentEvent(
            AssessmentEvent event,
            Message message,
            Channel channel
    ) {
        try {
            log.info("Received assessment event {} for user {}", event.getEventType(), event.getUserId());
            log.debug("Event data: {}", event);

            String title = "Assessment submitted";
            String content = "Your self assessment has been submitted";

            Notification notification = Notification.builder()
                    .category(NotificationCategory.INFO)
                    .content(content)
                    .recipientId(String.valueOf(event.getUserId()))
                    .title(title)
                    .type(NotificationType.IN_APP)
                    .eventType(NotificationEventType.ASSESSMENT)
                    .sentAt(LocalDateTime.now())
                    .build();

            Notification savedNotification = notificationServices.saveNotification(notification);
            handleAssessmentUpdates(savedNotification, event);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("Successfully processed assessment event: {} for user: {}",
                    event.getEventType(), event.getUserId());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.FEEDBACK_EVENTS_QUEUE)
    @Transactional
    public void handleFeedbackEvent(
            FeedbackEvent event,
            Message message,
            Channel channel
    ) {
        try {
            log.info("Received feedback event {} for user {}", event.getEventType(), event.getDeveloperId());
            log.debug("Event data: {}", event);

            String title = "New Feedback";
            String content = "Your manager has reviewed your performance and left a new feedback";

            Notification notification = Notification.builder()
                    .category(NotificationCategory.INFO)
                    .content(content)
                    .recipientId(String.valueOf(event.getDeveloperId()))
                    .title(title)
                    .type(NotificationType.IN_APP)
                    .eventType(NotificationEventType.FEEDBACK)
                    .sentAt(LocalDateTime.now())
                    .build();

            Notification savedNotification = notificationServices.saveNotification(notification);

            handleFeedBackUpdates(savedNotification, event);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("Successfully processed feedback event: {} for user: {}",
                    event.getEventType(), event.getDeveloperId());

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private void handleAssessmentUpdates(Notification notification, AssessmentEvent event) {
        if (notification.getType().equals(NotificationType.EMAIL)) {
            emailService.sendEmail(
                    event.getUserContext().getEmail(),
                    notification.getTitle(),
                    notification.getContent()
            );
            log.info("Email notification sent for assessment event to: {}",
                    event.getUserContext().getEmail());
        } else {
            sendWebSocketPush(notification);
        }

        log.info("Assessment notification processed");
    }

    private void handleFeedBackUpdates(Notification notification, FeedbackEvent event) {
        if (notification.getType().equals(NotificationType.EMAIL)) {
            emailService.sendEmail(
                    event.getDeveloperContext().getEmail(),
                    notification.getTitle(),
                    notification.getContent()
            );
            log.info("Email notification sent for feedback event to: {}",
                    event.getDeveloperContext().getEmail());
        } else {
            sendWebSocketPush(notification);
        }

        log.info("Feedback notification processed");
    }

    private void sendWebSocketPush(Notification notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(notification.getRecipientId()),
                    "/queue/notifications",
                    notification
            );
            log.info("WebSocket notification sent to user: {}", notification.getRecipientId());
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification", e);
        }
    }
}

