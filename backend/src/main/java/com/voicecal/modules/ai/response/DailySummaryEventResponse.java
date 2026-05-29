package com.voicecal.modules.ai.response;

import com.voicecal.common.enums.dao.EventCategory;
import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import java.time.LocalDateTime;

/**
 * 每日摘要中的轻量日程对象。
 *
 * @param id 日程 ID
 * @param title 日程标题
 * @param startTime 开始时间
 * @param endTime 结束时间
 * @param category 日程分类
 */
public record DailySummaryEventResponse(
        Long id,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        EventCategory category
) {

    /**
     * 将日程响应转换为每日摘要轻量日程对象。
     *
     * @param event 日程响应
     * @return 每日摘要轻量日程对象
     */
    public static DailySummaryEventResponse from(CalendarEventResponse event) {
        return new DailySummaryEventResponse(
                event.id(),
                event.title(),
                event.startTime(),
                event.endTime(),
                event.category() == null ? EventCategory.OTHER : event.category()
        );
    }
}
