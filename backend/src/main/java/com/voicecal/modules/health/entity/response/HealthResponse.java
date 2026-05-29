package com.voicecal.modules.health.entity.response;

import java.time.OffsetDateTime;

/**
 * 健康检查响应对象，描述后端服务当前运行状态。
 *
 * @param status 服务状态，例如 UP
 * @param currentTime 当前服务器时间
 * @param profile 当前运行环境
 */
public record HealthResponse(
        String status,
        OffsetDateTime currentTime,
        String profile
) {
}
