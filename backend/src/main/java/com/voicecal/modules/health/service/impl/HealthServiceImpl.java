package com.voicecal.modules.health.service.impl;

import com.voicecal.modules.health.response.HealthResponse;
import com.voicecal.modules.health.service.HealthService;
import java.time.OffsetDateTime;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * 健康检查服务实现，负责组装服务状态、当前时间和运行环境。
 */
@Service
public class HealthServiceImpl implements HealthService {

    private static final String DEFAULT_PROFILE = "default";

    private final Environment environment;

    /**
     * 创建健康检查服务实现。
     *
     * @param environment Spring 运行环境信息
     */
    public HealthServiceImpl(Environment environment) {
        this.environment = environment;
    }

    /**
     * 获取后端服务当前健康状态。
     *
     * @return 健康检查响应对象
     */
    @Override
    public HealthResponse getHealthStatus() {
        return new HealthResponse("UP", OffsetDateTime.now(), getActiveProfile());
    }

    private String getActiveProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        return activeProfiles.length == 0 ? DEFAULT_PROFILE : String.join(",", activeProfiles);
    }
}
