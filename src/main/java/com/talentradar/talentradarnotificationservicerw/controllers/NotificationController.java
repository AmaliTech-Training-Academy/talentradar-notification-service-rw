package com.talentradar.talentradarnotificationservicerw.controllers;

import com.talentradar.talentradarnotificationservicerw.domain.dtos.*;
import com.talentradar.talentradarnotificationservicerw.domain.entities.Notification;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationCategory;
import com.talentradar.talentradarnotificationservicerw.domain.mappers.EntityToDTO;
import com.talentradar.talentradarnotificationservicerw.services.NotificationServices;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationServices notificationService;

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO> getNotifications(@RequestHeader("X-User-Id") String userId, @RequestParam(required = false) NotificationCategory category, @RequestParam(required = false) String status, Pageable pageable) {
        Page<Notification> notificationsPage = notificationService.findNotifications(userId, Optional.of(category), Optional.of(status), pageable);
        return getPaginatedResponseDTOResponseEntity(notificationsPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SingleNotificationResponseDTO> getSingleNotification(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        Optional<SingleNotificationResponseDTO> foundNotification = notificationService.getNotification(id, userId);
        if (foundNotification.isEmpty()) {
            throw new EntityNotFoundException("Notification entity not found");
        }

        SingleNotificationResponseDTO notificationResponse = foundNotification.get();
        return new ResponseEntity<>(notificationResponse, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<PaginatedResponseDTO> searchNotification(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam String query, Pageable pageable) {
        Page<Notification> notificationsPage = notificationService.searchNotification(query, userId, pageable);
        return getPaginatedResponseDTOResponseEntity(notificationsPage);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<SimpleResponseDTO> readNotification(@PathVariable String id) {
        SimpleResponseDTO simpleResponse = notificationService.readNotification(id);

        return new ResponseEntity<>(simpleResponse, HttpStatus.OK);
    }

    @PatchMapping("/{id}/dismiss")
    public ResponseEntity<SimpleResponseDTO> dismissNotification(@PathVariable String id) {
        SimpleResponseDTO notificationResponse = notificationService.dismissNotification(id);
        return new ResponseEntity<>(
                notificationResponse,
                HttpStatus.OK);
    }

    private ResponseEntity<PaginatedResponseDTO> getPaginatedResponseDTOResponseEntity(Page<Notification> notificationsPage) {
        List<NotificationDTO> notificationList = notificationsPage.stream().map(EntityToDTO::notificationEntityToDTO).toList();

        PaginatedResponseDTO response = PaginatedResponseDTO.builder()
                .success(true)
                .message("Notifications retrieved successfully")
                .data(
                        NotificationsResponseData.builder()
                                .items(notificationList)
                                .pagination(
                                        PaginationSchema.builder()
                                                .page(notificationsPage.getNumber())
                                                .size(notificationsPage.getSize())
                                                .totalElements(notificationsPage.getTotalElements())
                                                .totalPages(notificationsPage.getTotalPages())
                                                .hasNext(notificationsPage.hasNext())
                                                .hasPrevious(notificationsPage.hasPrevious())
                                                .build()).build()).errors(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
