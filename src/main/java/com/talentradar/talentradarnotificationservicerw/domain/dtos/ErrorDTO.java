package com.talentradar.talentradarnotificationservicerw.domain.dtos;

import lombok.Builder;

@Builder
public record ErrorDTO(
        int status,
        String message
) {
}
