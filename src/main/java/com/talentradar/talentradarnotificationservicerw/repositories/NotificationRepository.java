package com.talentradar.talentradarnotificationservicerw.repositories;

import com.talentradar.talentradarnotificationservicerw.domain.entities.Notification;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findAll(Pageable pageable);

    Page<Notification> findAllByRecipientIdAndTitleOrContent(String recipientId, String title, String content, Pageable pageable);
}
