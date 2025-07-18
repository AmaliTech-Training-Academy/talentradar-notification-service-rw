package com.talentradar.talentradarnotificationservicerw.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentradar.talentradarnotificationservicerw.config.KafkaConsumer;
import com.talentradar.talentradarnotificationservicerw.domain.entities.Notification;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationCategory;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationEventType;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationType;
import com.talentradar.talentradarnotificationservicerw.domain.events.FeedBackServiceEvent;
import com.talentradar.talentradarnotificationservicerw.services.impl.NotificationServicesImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NotificationServicesImpl notificationServices;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private KafkaConsumer kafkaConsumer;

    @Test
    void shouldSaveNotificationAndSendViaWebSocket() throws Exception {
        String json = """
                    {
                      "category": "INFO",
                      "content": "This is a notification",
                      "recipientId": "user1",
                      "title": "New notification",
                      "type": "IN_APP"
                    }
                    """;

        FeedBackServiceEvent event = FeedBackServiceEvent.builder()
                .category(NotificationCategory.INFO)
                .content("This is a notification")
                .recipientId("user1")
                .title("New notification")
                .type(NotificationType.IN_APP)
                .build();

        Notification saved = Notification.builder()
                .id(UUID.randomUUID().toString())
                .recipientId("user1")
                .title("New notification")
                .content("This is a notification")
                .category(NotificationCategory.INFO)
                .type(NotificationType.IN_APP)
                .build();

        when(objectMapper.readValue(json, FeedBackServiceEvent.class)).thenReturn(event);
        when(notificationServices.saveNotification(any())).thenReturn(saved);

        kafkaConsumer.feedbackListener(json);

        verify(notificationServices).saveNotification(any());
        verify(messagingTemplate).convertAndSendToUser(
                eq("user1"),
                eq("/queue/notifications"),
                eq(saved)
        );
    }


}
