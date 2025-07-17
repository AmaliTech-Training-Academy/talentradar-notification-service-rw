package com.talentradar.talentradarnotificationservicerw.domain.dtos;

import lombok.Builder;

@Builder
public record SingleNotificationResponseDTO(
        boolean success,
        String message,
        NotificationDTO data,
        ErrorDTO errors
) {
}
