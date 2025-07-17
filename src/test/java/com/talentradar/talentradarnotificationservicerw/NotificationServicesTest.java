package com.talentradar.talentradarnotificationservicerw;

import com.talentradar.talentradarnotificationservicerw.domain.entities.Notification;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationCategory;
import com.talentradar.talentradarnotificationservicerw.repositories.NotificationRepository;
import com.talentradar.talentradarnotificationservicerw.services.impl.NotificationServicesImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class NotificationServicesTest {
    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private NotificationServicesImpl notificationServices;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Test
    void findNotifications_withCategoryAndStatusRead_shouldQueryCorrectly() {
        String recipient = "user1";
        NotificationCategory category = NotificationCategory.WARNING;
        String status = "READ";

        Notification notification = new Notification();
        List<Notification> notifications = List.of(notification);

        when(mongoTemplate.find(any(Query.class), eq(Notification.class))).thenReturn(notifications);
        when(mongoTemplate.count(any(Query.class), eq(Notification.class))).thenReturn(1L);

        Page<Notification> result = notificationServices.findNotifications(
                recipient,
                Optional.of(category),
                Optional.of(status),
                pageable
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(notification, result.getContent().get(0));
        verify(mongoTemplate).find(any(Query.class), eq(Notification.class));
        verify(mongoTemplate).count(any(Query.class), eq(Notification.class));
    }

    @Test
    void findNotifications_withoutCategoryAndUnreadStatus_shouldQueryCorrectly() {
        String recipient = "user2";
        String status = "UNREAD";

        Notification notification = new Notification();
        List<Notification> notifications = List.of(notification);

        when(mongoTemplate.find(any(Query.class), eq(Notification.class))).thenReturn(notifications);
        when(mongoTemplate.count(any(Query.class), eq(Notification.class))).thenReturn(1L);

        Page<Notification> result = notificationServices.findNotifications(
                recipient,
                Optional.empty(),
                Optional.of(status),
                pageable
        );

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getNotification_existingId_shouldReturnNotification() {
        String id = "notif123";
        Notification notification = new Notification();
        when(notificationRepository.findById(id)).thenReturn(Optional.of(notification));

        Optional<Notification> result = notificationServices.getNotification(id);
        assertTrue(result.isPresent());
    }

    @Test
    void searchNotification_shouldDelegateToRepository() {
        String userId = "user123";
        String searchTerm = "important";

        Notification notification = new Notification();
        Page<Notification> expectedPage = new PageImpl<>(List.of(notification));
        when(notificationRepository.findAllByRecipientIdAndTitleOrContent(userId, searchTerm, searchTerm, pageable))
                .thenReturn(expectedPage);

        Page<Notification> result = notificationServices.searchNotification(searchTerm, userId, pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void readNotification_existingId_shouldMarkAsRead() {
        String id = "notif456";
        Notification notification = new Notification();
        when(notificationRepository.findById(id)).thenReturn(Optional.of(notification));

        notificationServices.readNotification(id);

        assertNotNull(notification.getReadAt());
        verify(notificationRepository).save(notification);
    }

    @Test
    void readNotification_invalidId_shouldThrowException() {
        when(notificationRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> notificationServices.readNotification("invalid"));
    }

    @Test
    void saveNotification_shouldSaveAndReturn() {
        Notification notification = new Notification();
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification result = notificationServices.saveNotification(notification);
        assertEquals(notification, result);
    }

    @Test
    void dismissNotification_existingId_shouldSetDismissedTrue() {
        String id = "notif789";
        Notification notification = new Notification();
        when(notificationRepository.findById(id)).thenReturn(Optional.of(notification));

        notificationServices.dismissNotification(id);

        assertTrue(notification.isDismissed());
        verify(notificationRepository).save(notification);
    }

    @Test
    void dismissNotification_invalidId_shouldThrowException() {
        when(notificationRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> notificationServices.dismissNotification("invalid"));
    }
}
