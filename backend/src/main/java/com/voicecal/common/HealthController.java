package com.voicecal.common;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final Environment environment;

    public HealthController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/api/health")
    public ApiResponse<HealthResponse> health() {
        String[] activeProfiles = environment.getActiveProfiles();
        List<String> profiles = activeProfiles.length == 0
                ? List.of("default")
                : Arrays.asList(activeProfiles);

        return ApiResponse.success(new HealthResponse("UP", OffsetDateTime.now(), profiles));
    }
}
