package com.voicecal.modules.ai.response;

import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import java.util.List;

/**
 * 每日摘要响应对象。
 *
 * @param date 摘要日期
 * @param timezone 时区 ID
 * @param eventCount 日程数量
 * @param busyMinutes 已安排分钟数
 * @param summary 摘要文案
 * @param events 当天日程列表
 */
public record DailySummaryResponse(
        String date,
        String timezone,
        int eventCount,
        long busyMinutes,
        String summary,
        List<CalendarEventResponse> events
) {
}
