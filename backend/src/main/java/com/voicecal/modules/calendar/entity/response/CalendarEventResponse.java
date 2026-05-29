package com.voicecal.modules.calendar.entity.response;

import com.voicecal.dao.entity.CalendarEvent;
import java.time.LocalDateTime;

/**
 * 日历事件响应对象。
 *
 * @param id 日程 ID
 * @param title 日程标题
 * @param description 日程描述
 * @param startTime 开始时间
 * @param endTime 结束时间
 * @param location 地点
 * @param reminderMinutes 提前提醒分钟数
 * @param reminderTriggered 提醒是否已触发
 * @param remindedAt 提醒触发时间
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record CalendarEventResponse(
        Long id,
        String title,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String location,
        Integer reminderMinutes,
        Boolean reminderTriggered,
        LocalDateTime remindedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /**
     * 将日程实体转换为接口响应对象。
     *
     * @param event 日程实体
     * @return 日程响应对象
     */
    public static CalendarEventResponse from(CalendarEvent event) {
        return new CalendarEventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation(),
                event.getReminderMinutes(),
                event.getReminderTriggered(),
                event.getRemindedAt(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
