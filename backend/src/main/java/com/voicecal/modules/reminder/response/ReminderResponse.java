package com.voicecal.modules.reminder.response;

import com.voicecal.dao.entity.CalendarEvent;
import java.time.LocalDateTime;

/**
 * 已触发提醒的日程响应对象。
 *
 * @param eventId 日程 ID
 * @param title 日程标题
 * @param startTime 日程开始时间
 * @param reminderMinutes 提前提醒分钟数
 * @param reminderTriggered 提醒是否已触发
 * @param remindedAt 提醒触发时间
 */
public record ReminderResponse(
        Long eventId,
        String title,
        LocalDateTime startTime,
        Integer reminderMinutes,
        Boolean reminderTriggered,
        LocalDateTime remindedAt
) {

    /**
     * 将日程实体转换为提醒响应对象。
     *
     * @param event 日程实体
     * @return 提醒响应对象
     */
    public static ReminderResponse from(CalendarEvent event) {
        return new ReminderResponse(
                event.getId(),
                event.getTitle(),
                event.getStartTime(),
                event.getReminderMinutes(),
                event.getReminderTriggered(),
                event.getRemindedAt()
        );
    }
}
