package com.voicecal.modules.ai.service.impl;

import com.voicecal.modules.ai.response.DailySummaryResponse;
import com.voicecal.modules.ai.service.DailySummaryService;
import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import com.voicecal.modules.calendar.service.CalendarEventQueryService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 每日摘要服务实现，使用确定性规则生成摘要，不依赖外部模型。
 */
@Service
public class DailySummaryServiceImpl implements DailySummaryService {

    private final CalendarEventQueryService calendarEventQueryService;

    public DailySummaryServiceImpl(CalendarEventQueryService calendarEventQueryService) {
        this.calendarEventQueryService = calendarEventQueryService;
    }

    /**
     * 生成指定日期的每日摘要。
     *
     * @param date 日期字符串，可为空
     * @param timezone 时区 ID，可为空
     * @return 每日摘要
     */
    @Override
    public DailySummaryResponse getDailySummary(String date, String timezone) {
        ZoneId zoneId = calendarEventQueryService.resolveZoneId(timezone);
        LocalDate summaryDate = calendarEventQueryService.resolveDate(date, zoneId);
        List<CalendarEventResponse> events = calendarEventQueryService.getEventsForDate(summaryDate, zoneId);
        long busyMinutes = calculateBusyMinutes(events);

        return new DailySummaryResponse(
                summaryDate.toString(),
                zoneId.getId(),
                events.size(),
                busyMinutes,
                buildSummary(summaryDate, events, busyMinutes),
                events
        );
    }

    private long calculateBusyMinutes(List<CalendarEventResponse> events) {
        return events.stream()
                .mapToLong(event -> Duration.between(event.startTime(), event.endTime()).toMinutes())
                .filter(duration -> duration > 0)
                .sum();
    }

    private String buildSummary(LocalDate date, List<CalendarEventResponse> events, long busyMinutes) {
        if (events.isEmpty()) {
            return date + " 没有已安排日程。";
        }
        CalendarEventResponse firstEvent = events.get(0);
        return date + " 共有 " + events.size() + " 个日程，已占用约 " + busyMinutes
                + " 分钟。第一个日程从 " + firstEvent.startTime().toLocalTime() + " 开始。";
    }
}
