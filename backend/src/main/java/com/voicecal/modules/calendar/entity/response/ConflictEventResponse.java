package com.voicecal.modules.calendar.entity.response;

import com.voicecal.dao.entity.CalendarEvent;
import java.time.LocalDateTime;

/**
 * 冲突日程响应对象。
 *
 * @param id 日程 ID
 * @param title 日程标题
 * @param startTime 开始时间
 * @param endTime 结束时间
 * @param location 地点
 */
public record ConflictEventResponse(
        Long id,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String location
) {

    /**
     * 将日程实体转换为冲突日程响应。
     *
     * @param event 日程实体
     * @return 冲突日程响应
     */
    public static ConflictEventResponse from(CalendarEvent event) {
        return new ConflictEventResponse(
                event.getId(),
                event.getTitle(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation()
        );
    }
}
