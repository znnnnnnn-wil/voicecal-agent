package com.voicecal.modules.health.service;

import com.voicecal.modules.health.entity.response.HealthResponse;

/**
 * 健康检查服务，定义服务运行状态查询能力。
 */
public interface HealthService {

    /**
     * 获取后端服务当前健康状态。
     *
     * @return 健康检查响应对象
     */
    HealthResponse getHealthStatus();
}
