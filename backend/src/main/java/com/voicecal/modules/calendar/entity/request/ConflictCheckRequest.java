package com.voicecal.modules.calendar.entity.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 日程冲突检测请求参数。
 *
 * @param startTime 待检测开始时间
 * @param endTime 待检测结束时间
 * @param excludeEventId 需要排除的日程 ID，更新日程时使用
 */
public record ConflictCheckRequest(
        @NotNull(message = "开始时间不能为空")
        LocalDateTime startTime,

        @NotNull(message = "结束时间不能为空")
        LocalDateTime endTime,

        Long excludeEventId
) {
}
