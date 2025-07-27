package com.talentradar.talentradarnotificationservicerw.events;

import com.rabbitmq.client.Channel;
import com.talentradar.talentradarnotificationservicerw.config.NotificationEventListener;
import com.talentradar.talentradarnotificationservicerw.domain.entities.Notification;
import com.talentradar.talentradarnotificationservicerw.domain.enums.AssessmentEventType;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationCategory;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationEventType;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationType;
import com.talentradar.talentradarnotificationservicerw.domain.events.AssessmentEvent;
import com.talentradar.talentradarnotificationservicerw.domain.events.FeedbackEvent;
import com.talentradar.talentradarnotificationservicerw.domain.events.FeedbackEventType;
import com.talentradar.talentradarnotificationservicerw.domain.events.UserContext;
import com.talentradar.talentradarnotificationservicerw.services.EmailService;
import com.talentradar.talentradarnotificationservicerw.services.NotificationServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationsEventListenerTest {

    @Mock
    private NotificationServices notificationServices;

    @Mock
    private EmailService emailService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private Channel channel;

    @Mock
    private Message message;

    @Mock
    private MessageProperties messageProperties;

    @InjectMocks
    private NotificationEventListener notificationEventListener;

    @BeforeEach
    void setUp() {
        when(message.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getDeliveryTag()).thenReturn(1L);
    }

    @Test
    void handleAssessmentEvent_ShouldCreateInAppNotification_WhenEventReceived() throws Exception {
        // Arrange
        UUID userId = UUID.fromString("01fae1aa-6b15-11f0-be17-325096b39f47");
        AssessmentEvent event = createAssessmentEvent(userId);
        Notification savedNotification = createExpectedAssessmentNotification(userId);

        when(notificationServices.saveNotification(any(Notification.class)))
                .thenReturn(savedNotification);

        // Act
        notificationEventListener.handleAssessmentEvent(event, message, channel);

        // Assert
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationServices).saveNotification(notificationCaptor.capture());

        Notification capturedNotification = notificationCaptor.getValue();
        assertEquals(NotificationCategory.INFO, capturedNotification.getCategory());
        assertEquals("Assessment submitted", capturedNotification.getTitle());
        assertEquals("Your self assessment has been submitted", capturedNotification.getContent());
        assertEquals(String.valueOf(userId), capturedNotification.getRecipientId());
        assertEquals(NotificationType.IN_APP, capturedNotification.getType());
        assertEquals(NotificationEventType.ASSESSMENT, capturedNotification.getEventType());
        assertNotNull(capturedNotification.getSentAt());

        verify(channel).basicAck(1L, false);
        verify(messagingTemplate).convertAndSendToUser(
                eq(String.valueOf(userId)),
                eq("/queue/notifications"),
                eq(savedNotification)
        );
        verifyNoInteractions(emailService);
    }

    @Test
    void handleAssessmentEvent_ShouldSendEmail_WhenNotificationTypeIsEmail() throws Exception {
        // Arrange
        UUID userId = UUID.fromString("01fae1aa-6b15-11f0-be17-325096b39f47");
        String userEmail = "jdoe@example.com";
        AssessmentEvent event = createAssessmentEventWithEmail(userId, userEmail);
        Notification savedNotification = createEmailNotification(userId);

        when(notificationServices.saveNotification(any(Notification.class)))
                .thenReturn(savedNotification);

        // Act
        notificationEventListener.handleAssessmentEvent(event, message, channel);

        // Assert
        verify(emailService).sendEmail(
                eq(userEmail),
                eq("Test title"),
                eq("Test content")
        );
        verify(channel).basicAck(1L, false);
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void handleFeedbackEvent_ShouldCreateInAppNotification_WhenEventReceived() throws Exception {
        // Arrange
        UUID developerId = UUID.fromString("01fae1aa-6b15-11f0-be17-325096b39f47");
        FeedbackEvent event = createFeedbackEvent(developerId);
        Notification savedNotification = createExpectedFeedbackNotification(developerId);

        when(notificationServices.saveNotification(any(Notification.class)))
                .thenReturn(savedNotification);

        // Act
        notificationEventListener.handleFeedbackEvent(event, message, channel);

        // Assert
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationServices).saveNotification(notificationCaptor.capture());

        Notification capturedNotification = notificationCaptor.getValue();
        assertEquals(NotificationCategory.INFO, capturedNotification.getCategory());
        assertEquals("New Feedback", capturedNotification.getTitle());
        assertEquals("Your manager has reviewed your performance and left a new feedback", capturedNotification.getContent());
        assertEquals(String.valueOf(developerId), capturedNotification.getRecipientId());
        assertEquals(NotificationType.IN_APP, capturedNotification.getType());
        assertEquals(NotificationEventType.FEEDBACK, capturedNotification.getEventType());
        assertNotNull(capturedNotification.getSentAt());

        verify(channel).basicAck(1L, false);
        verify(messagingTemplate).convertAndSendToUser(
                eq(String.valueOf(developerId)),
                eq("/queue/notifications"),
                eq(savedNotification)
        );
        verifyNoInteractions(emailService);
    }

    private AssessmentEvent createAssessmentEvent(UUID userId) {
        AssessmentEvent event = mock(AssessmentEvent.class);
        when(event.getUserId()).thenReturn(userId);
        when(event.getEventType()).thenReturn(AssessmentEventType.ASSESSMENT_SUBMITTED);
        return event;
    }

    private AssessmentEvent createAssessmentEventWithEmail(UUID userId, String email) {
        UserContext userContext = UserContext.builder()
                .userId(userId)
                .fullName("John Doe")
                .username("jdoe")
                .email(email)
                .role("DEVELOPER")
                .managerId(UUID.fromString("1cabe2ba-6b15-11f0-9d1b-325096b39f47"))
                .build();

        AssessmentEvent event = AssessmentEvent.builder()
                .eventType(AssessmentEventType.ASSESSMENT_SUBMITTED)
                .assessmentId(UUID.fromString("309534ca-6b15-11f0-bdf5-325096b39f47"))
                .userId(userContext.getUserId())
                .reflection("Reflection of the assessment")
                .averageScore(4)
                .submissionStatus("SUBMITTED")
                .timestamp(LocalDateTime.now())
                .eventId(UUID.randomUUID().toString())
                .source("assessment-service")
                .userContext(userContext)
                .build();

        return event;
    }

    private FeedbackEvent createFeedbackEvent(UUID developerId) {
        FeedbackEvent event = mock(FeedbackEvent.class);
        when(event.getDeveloperId()).thenReturn(developerId);
        when(event.getEventType()).thenReturn(FeedbackEventType.valueOf("FEEDBACK_CREATED"));
        return event;
    }

    private FeedbackEvent createFeedbackEventWithEmail(UUID developerId, String email) {
        UserContext developerContext = UserContext.builder()
                .userId(developerId)
                .fullName("John Doe")
                .username("jdoe")
                .email(email)
                .role("DEVELOPER")
                .managerId(UUID.fromString("1cabe2ba-6b15-11f0-9d1b-325096b39f47"))
                .build();


        FeedbackEvent event = createFeedbackEvent(developerId);
        when(developerContext.getEmail()).thenReturn(email);
        when(event.getDeveloperContext()).thenReturn(developerContext);
        return event;
    }

    private Notification createExpectedAssessmentNotification(UUID userId) {
        return Notification.builder()
                .category(NotificationCategory.INFO)
                .content("Your self assessment has been submitted")
                .recipientId(String.valueOf(userId))
                .title("Assessment submitted")
                .type(NotificationType.IN_APP)
                .eventType(NotificationEventType.ASSESSMENT)
                .sentAt(LocalDateTime.now())
                .build();
    }

    private Notification createExpectedFeedbackNotification(UUID developerId) {
        return Notification.builder()
                .category(NotificationCategory.INFO)
                .content("Your manager has reviewed your performance and left a new feedback")
                .recipientId(String.valueOf(developerId))
                .title("New Feedback")
                .type(NotificationType.IN_APP)
                .eventType(NotificationEventType.FEEDBACK)
                .sentAt(LocalDateTime.now())
                .build();
    }

    private Notification createEmailNotification(UUID userId) {
        return Notification.builder()
                .category(NotificationCategory.INFO)
                .content("Test content")
                .recipientId(String.valueOf(userId))
                .title("Test title")
                .type(NotificationType.EMAIL)
                .eventType(NotificationEventType.ASSESSMENT)
                .sentAt(LocalDateTime.now())
                .build();
    }
}