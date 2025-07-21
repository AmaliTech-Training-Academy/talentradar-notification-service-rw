package com.talentradar.talentradarnotificationservicerw.services.impl;

import com.talentradar.talentradarnotificationservicerw.domain.dtos.SimpleResponseDTO;
import com.talentradar.talentradarnotificationservicerw.domain.dtos.SingleNotificationResponseDTO;
import com.talentradar.talentradarnotificationservicerw.domain.entities.Notification;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationCategory;
import com.talentradar.talentradarnotificationservicerw.domain.mappers.EntityToDTO;
import com.talentradar.talentradarnotificationservicerw.repositories.NotificationRepository;
import com.talentradar.talentradarnotificationservicerw.services.NotificationServices;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationServicesImpl implements NotificationServices {
    private final NotificationRepository notificationRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Notification> findNotifications(
            String recipient,
            Optional<NotificationCategory> category,
            Optional<String> status,
            Pageable pageable
    ) {
        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.add(Criteria.where("recipientId").is(recipient));

        // Apply category filter only if present
        category.ifPresent(cat -> criteriaList.add(Criteria.where("category").is(cat)));
        // Handle read/unread status
        status.ifPresent(s -> {
            if (s.equalsIgnoreCase("READ")) {
                criteriaList.add(Criteria.where("readAt").ne(null));
            } else if (s.equalsIgnoreCase("UNREAD")) {
                criteriaList.add(Criteria.where("readAt").is(null));
            }
        });

        criteriaList.add(Criteria.where("dismissed").ne(true));

        Criteria finalCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));

        Query query = new Query(finalCriteria);

        // Default sort if none provided
        if (!pageable.getSort().isSorted()) {
            query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        // Apply pagination and sorting
        query.with(pageable);

        // Execute query
        List<Notification> notifications = mongoTemplate.find(query, Notification.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Notification.class);

        return new PageImpl<>(notifications, pageable, total);
    }


    @Override
    public Optional<SingleNotificationResponseDTO> getNotification(String notificationId, String userId) {
        Optional<Notification> foundNotification = notificationRepository.findByIdAndRecipientId(notificationId, userId);
        return foundNotification.map(notification -> SingleNotificationResponseDTO.builder()
                .success(true)
                .message("Notification retrieved")
                .data(EntityToDTO.notificationEntityToDTO(notification))
                .errors(null)
                .build());
    }

    @Override
    public Page<Notification> searchNotification(String searchTerm, String userId, Pageable pageable) {
        return notificationRepository.findAllByRecipientIdAndTitleOrContentContainingIgnoreCase(userId, searchTerm, searchTerm, pageable);
    }

    @Override
    public SimpleResponseDTO readNotification(String notificationId) {
        Optional<Notification> foundNotification = notificationRepository.findById(notificationId);

        if (foundNotification.isEmpty()) {
            throw new EntityNotFoundException("Notification entity not found");
        }

        Notification notification = foundNotification.get();
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
        return SimpleResponseDTO.builder()
                .success(true)
                .message("Notification marked as read")
                .data(null)
                .errors(null)
                .build();
    }

    @Override
    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public SimpleResponseDTO dismissNotification(String notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);

        if (optionalNotification.isEmpty()) {
            throw new EntityNotFoundException("Notification entity not found");
        }

        Notification notification = optionalNotification.get();
        notification.setDismissed(true);
        notificationRepository.save(notification);

        return SimpleResponseDTO.builder()
                .success(true)
                .message("Notification dismissed")
                .errors(null)
                .build();
    }
}
