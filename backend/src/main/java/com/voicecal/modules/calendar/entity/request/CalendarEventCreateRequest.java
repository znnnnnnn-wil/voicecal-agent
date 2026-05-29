package com.voicecal.modules.calendar.entity.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 创建日历事件请求参数。
 *
 * @param title 日程标题
 * @param description 日程描述
 * @param startTime 开始时间
 * @param endTime 结束时间
 * @param location 地点
 * @param reminderMinutes 提前提醒分钟数
 */
public record CalendarEventCreateRequest(
        @NotBlank(message = "日程标题不能为空")
        @Size(max = 100, message = "日程标题不能超过 100 个字符")
        String title,

        @Size(max = 1000, message = "日程描述不能超过 1000 个字符")
        String description,

        @NotNull(message = "开始时间不能为空")
        LocalDateTime startTime,

        @NotNull(message = "结束时间不能为空")
        LocalDateTime endTime,

        @Size(max = 255, message = "地点不能超过 255 个字符")
        String location,

        @Min(value = 0, message = "提醒时间不能小于 0")
        Integer reminderMinutes
) {
}
