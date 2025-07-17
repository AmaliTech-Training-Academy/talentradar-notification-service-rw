package com.talentradar.talentradarnotificationservicerw.domain.dtos;

import lombok.Builder;

@Builder
public record ApiErrorResponse(
        boolean success,
        String errors,
        String message
) {
}
