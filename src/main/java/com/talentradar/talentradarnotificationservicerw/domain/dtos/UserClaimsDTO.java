package com.talentradar.talentradarnotificationservicerw.domain.dtos;

import lombok.Builder;

@Builder
public record UserClaimsDTO(
        String userId,
        String email,
        String fullName,
        String role
) {
}
