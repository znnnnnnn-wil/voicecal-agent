package com.voicecal.modules.ai.response;

import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import java.util.List;
import java.util.Map;

/**
 * 每日摘要响应对象。
 *
 * @param date 摘要日期
 * @param timezone 时区 ID
 * @param eventCount 日程数量
 * @param busyMinutes 已安排分钟数
 * @param categoryStats 日程分类统计
 * @param earliestEvent 最早开始的日程
 * @param latestEvent 最晚结束的日程
 * @param summary 摘要文案
 * @param events 当天日程列表
 */
public record DailySummaryResponse(
        String date,
        String timezone,
        int eventCount,
        long busyMinutes,
        Map<String, Long> categoryStats,
        DailySummaryEventResponse earliestEvent,
        DailySummaryEventResponse latestEvent,
        String summary,
        List<CalendarEventResponse> events
) {
}
