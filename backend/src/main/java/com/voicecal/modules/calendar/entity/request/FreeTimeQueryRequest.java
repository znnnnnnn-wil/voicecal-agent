package com.voicecal.modules.calendar.entity.request;

import java.time.LocalDateTime;

/**
 * 空闲时间查询请求参数。
 *
 * @param startTime 查询开始时间
 * @param endTime 查询结束时间
 * @param minMinutes 最小空闲分钟数
 */
public record FreeTimeQueryRequest(
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer minMinutes
) {
}
