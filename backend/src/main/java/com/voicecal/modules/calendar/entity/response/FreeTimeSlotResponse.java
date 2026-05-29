package com.voicecal.modules.calendar.entity.response;

import java.time.LocalDateTime;

/**
 * 空闲时间段响应对象。
 *
 * @param startTime 空闲开始时间
 * @param endTime 空闲结束时间
 * @param minutes 空闲分钟数
 */
public record FreeTimeSlotResponse(
        LocalDateTime startTime,
        LocalDateTime endTime,
        long minutes
) {
}
