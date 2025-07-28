package com.talentradar.talentradarnotificationservicerw.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentradar.talentradarnotificationservicerw.domain.dtos.*;
import com.talentradar.talentradarnotificationservicerw.domain.entities.Notification;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationCategory;
import com.talentradar.talentradarnotificationservicerw.services.NotificationServices;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationsControllerTest {

    @Mock
    private NotificationServices notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Notification sampleNotification;
    private NotificationDTO sampleNotificationDTO;
    private SingleNotificationResponseDTO sampleSingleNotificationResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
        objectMapper = new ObjectMapper();

        // Setup sample data
        sampleNotification = createSampleNotification();
        sampleNotificationDTO = createSampleNotificationDTO();
        sampleSingleNotificationResponse = createSampleSingleNotificationResponse();
    }

    @Test
    void getSingleNotification_WithValidId_ReturnsNotification() throws Exception {
        // Given
        String userId = "user123";
        String notificationId = "notification123";

        when(notificationService.getNotification(notificationId, userId))
                .thenReturn(Optional.of(sampleSingleNotificationResponse));

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/{id}", notificationId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(sampleSingleNotificationResponse.data().id()));

        verify(notificationService).getNotification(notificationId, userId);
    }
    @Test
    void readNotification_WithValidId_ReturnsSuccessResponse() throws Exception {
        // Given
        String notificationId = "notification123";
        SimpleResponseDTO expectedResponse = SimpleResponseDTO.builder()
                .success(true)
                .message("Notification marked as read")
                .build();

        when(notificationService.readNotification(notificationId))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification marked as read"));

        verify(notificationService).readNotification(notificationId);
    }

    @Test
    void dismissNotification_WithValidId_ReturnsSuccessResponse() throws Exception {
        // Given
        String notificationId = "notification123";
        SimpleResponseDTO expectedResponse = SimpleResponseDTO.builder()
                .success(true)
                .message("Notification dismissed")
                .build();

        when(notificationService.dismissNotification(notificationId))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(patch("/api/v1/notifications/{id}/dismiss", notificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification dismissed"));

        verify(notificationService).dismissNotification(notificationId);
    }

    @Test
    void parseEnum_WithInvalidValue_ReturnsEmptyOptional() {
        // Given
        String invalidCategory = "INVALID";

        // When
        Optional<NotificationCategory> result = notificationController.parseEnum(NotificationCategory.class, invalidCategory);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void parseEnum_WithNullValue_ReturnsEmptyOptional() {
        // When
        Optional<NotificationCategory> result = notificationController.parseEnum(NotificationCategory.class, null);

        // Then
        assertTrue(result.isEmpty());
    }
    // Test direct controller methods without MockMvc for better unit testing
    @Test
    void getNotifications_DirectCall_ReturnsCorrectResponse() {
        // Given
        String userId = "user123";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> notificationsPage = new PageImpl<>(Arrays.asList(sampleNotification), pageable, 1);

        when(notificationService.findNotifications(
                eq(userId),
                any(Optional.class),
                any(Optional.class),
                eq(pageable)
        )).thenReturn(notificationsPage);

        // When
        ResponseEntity<PaginatedResponseDTO> response = notificationController.getNotifications(
                userId, null, null, pageable
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
        assertEquals("Notifications retrieved successfully", response.getBody().message());
        assertNull(response.getBody().errors());

        NotificationsResponseData data = (NotificationsResponseData) response.getBody().data();
        assertEquals(1, data.items().size());
        assertEquals(0, data.pagination().page());
        assertEquals(10, data.pagination().size());
        assertEquals(1, data.pagination().totalElements());
        assertEquals(1, data.pagination().totalPages());
    }

    @Test
    void getSingleNotification_DirectCall_ReturnsCorrectResponse() {
        // Given
        String userId = "user123";
        String notificationId = "notification123";

        when(notificationService.getNotification(notificationId, userId))
                .thenReturn(Optional.of(sampleSingleNotificationResponse));

        // When
        ResponseEntity<SingleNotificationResponseDTO> response = notificationController.getSingleNotification(userId, notificationId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(sampleSingleNotificationResponse, response.getBody());
    }

    @Test
    void getSingleNotification_DirectCall_ThrowsEntityNotFoundException() {
        // Given
        String userId = "user123";
        String notificationId = "invalid123";

        when(notificationService.getNotification(notificationId, userId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            notificationController.getSingleNotification(userId, notificationId);
        });
    }

    // Helper methods to create sample data
    private Notification createSampleNotification() {
        Notification notification = new Notification();
        notification.setId("notification123");
        notification.setTitle("Test Notification");
        notification.setContent("This is a test notification");
        notification.setCategory(NotificationCategory.SUCCESS);
        notification.setRecipientId("user123");
        notification.setReadAt(null);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    private NotificationDTO createSampleNotificationDTO() {
        return NotificationDTO.builder()
                .id("notification123")
                .title("Test Notification")
                .content("This is a test notification")
                .category(NotificationCategory.valueOf("SUCCESS"))
                .readAt(null)
                .sentAt(LocalDateTime.now())
                .build();
    }

    private SingleNotificationResponseDTO createSampleSingleNotificationResponse() {
        return SingleNotificationResponseDTO.builder()
                .success(true)
                .message("Notification retrieved successfully")
                .data(sampleNotificationDTO)
                .errors(null)
                .build();
    }
}
