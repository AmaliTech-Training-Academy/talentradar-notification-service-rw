package com.talentradar.talentradarnotificationservicerw.domain.events;

import com.talentradar.talentradarnotificationservicerw.domain.events.FeedBackServiceEvent;
import lombok.Builder;

@Builder
public record EventLog(
        String trigger,
        FeedBackServiceEvent event
) {
}
