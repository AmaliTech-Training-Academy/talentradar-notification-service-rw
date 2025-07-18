package com.talentradar.talentradarnotificationservicerw.domain.dtos;

import lombok.Builder;

@Builder
public record EventLog(
        String trigger,
        String outCome
) {
}
