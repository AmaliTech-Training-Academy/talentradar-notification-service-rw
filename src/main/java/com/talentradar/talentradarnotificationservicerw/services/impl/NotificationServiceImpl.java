package com.talentradar.talentradarnotificationservicerw.services.impl;

import com.talentradar.talentradarnotificationservicerw.domain.entities.Notification;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationCategory;
import com.talentradar.talentradarnotificationservicerw.repositories.NotificationRepository;
import com.talentradar.talentradarnotificationservicerw.services.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Notification> findNotifications(String recipient, Optional<NotificationCategory> category, Optional<String> status, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("recipientId").is(recipient));
        category.ifPresent(cat -> criteriaList.add(Criteria.where("category").is(cat)));

        status.ifPresent(s -> {
            if (s.equalsIgnoreCase("READ")) {
                criteriaList.add(Criteria.where("readAt").ne(null));
            } else if (s.equalsIgnoreCase("UNREAD")) {
                criteriaList.add(Criteria.where("readAt").is(null));
            }
        });

        Query query = new Query(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])))
                .with(pageable);
        List<Notification> notifications = mongoTemplate.find(query, Notification.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Notification.class);
        return new PageImpl<>(notifications, pageable, total);
    }

    @Override
    public Optional<Notification> getNotification(String notificationId) {
        return notificationRepository.findById(notificationId);
    }

    @Override
    public Page<Notification> searchNotification(String searchTerm, String userId, Pageable pageable) {
        return notificationRepository.findAllByRecipientIdAndTitleOrContent(userId, searchTerm, searchTerm, pageable);
    }

    @Override
    public void readNotification(String notificationId) {
        Optional<Notification> optionalNotification = getNotification(notificationId);

        if (optionalNotification.isEmpty()) {
            throw new EntityNotFoundException("Notification entity not found");
        }

        Notification notification = optionalNotification.get();
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public void dismissNotification(String notificationId) {
        Optional<Notification> optionalNotification = getNotification(notificationId);

        if (optionalNotification.isEmpty()) {
            throw new EntityNotFoundException("Notification entity not found");
        }

        Notification notification = optionalNotification.get();
        notification.setDismissed(true);
        notificationRepository.save(notification);
    }
}
