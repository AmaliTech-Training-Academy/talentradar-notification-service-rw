package com.talentradar.talentradarnotificationservicerw.domain.dtos;

import lombok.Builder;

@Builder
public record PaginatedResponseDTO(
        boolean success,
        String message,
        NotificationsResponseData data,
        ErrorDTO errors
) {
}
