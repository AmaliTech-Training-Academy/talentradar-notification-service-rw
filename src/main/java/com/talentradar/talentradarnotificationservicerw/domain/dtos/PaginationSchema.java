package com.talentradar.talentradarnotificationservicerw.domain.dtos;

import lombok.Builder;

@Builder
public record PaginationSchema(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
