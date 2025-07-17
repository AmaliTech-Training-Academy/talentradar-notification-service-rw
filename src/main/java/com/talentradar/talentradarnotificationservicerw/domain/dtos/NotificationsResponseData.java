package com.talentradar.talentradarnotificationservicerw.domain.dtos;

import lombok.Builder;

import java.util.List;

@Builder
public record NotificationsResponseData(
        List<NotificationDTO> notifications,
        PaginationSchema pagination
) {
}
