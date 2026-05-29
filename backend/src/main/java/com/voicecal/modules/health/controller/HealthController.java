package com.voicecal.modules.health.controller;

import com.voicecal.common.response.ApiResponse;
import com.voicecal.modules.health.response.HealthResponse;
import com.voicecal.modules.health.service.HealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口控制器，只负责接收请求并返回服务层结果。
 */
@RestController
public class HealthController {

    private final HealthService healthService;

    /**
     * 创建健康检查控制器。
     *
     * @param healthService 健康检查服务
     */
    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    /**
     * 查询后端服务运行状态。
     *
     * @return 服务状态、当前服务器时间和运行环境
     */
    @GetMapping("/api/health")
    public ApiResponse<HealthResponse> health() {
        return ApiResponse.success(healthService.getHealthStatus());
    }
}
