package com.voicecal.common;

import java.time.OffsetDateTime;
import java.util.List;

public record HealthResponse(
        String status,
        OffsetDateTime currentTime,
        List<String> activeProfiles
) {
}
