package com.talentradar.talentradarnotificationservicerw.domain.events;

import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationCategory;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationType;
import lombok.Builder;

@Builder
public record FeedBackServiceEvent(
        String title,
        String content,
        String recipientId,
        String recipientEmail,
        NotificationType type,
        NotificationCategory category

) {
}
