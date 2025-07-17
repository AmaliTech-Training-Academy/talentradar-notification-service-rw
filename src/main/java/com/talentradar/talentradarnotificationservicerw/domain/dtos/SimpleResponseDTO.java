package com.talentradar.talentradarnotificationservicerw.domain.dtos;

import lombok.Builder;

@Builder
public record SimpleResponseDTO(
        boolean success,
        String message,
        NotificationsResponseData data,
        ErrorDTO errors
) {
}
