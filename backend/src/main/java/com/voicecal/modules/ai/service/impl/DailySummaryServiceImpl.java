package com.voicecal.modules.ai.service.impl;

import com.voicecal.common.enums.dao.EventCategory;
import com.voicecal.modules.ai.response.DailySummaryEventResponse;
import com.voicecal.modules.ai.response.DailySummaryResponse;
import com.voicecal.modules.ai.service.DailySummaryService;
import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import com.voicecal.modules.calendar.service.CalendarEventQueryService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
        Map<String, Long> categoryStats = buildCategoryStats(events);
        DailySummaryEventResponse earliestEvent = events.isEmpty() ? null : DailySummaryEventResponse.from(events.get(0));
        DailySummaryEventResponse latestEvent = events.stream()
                .max(Comparator.comparing(CalendarEventResponse::endTime))
                .map(DailySummaryEventResponse::from)
                .orElse(null);

        return new DailySummaryResponse(
                summaryDate.toString(),
                zoneId.getId(),
                events.size(),
                busyMinutes,
                categoryStats,
                earliestEvent,
                latestEvent,
                buildSummary(summaryDate, events, busyMinutes, categoryStats),
                events
        );
    }

    private long calculateBusyMinutes(List<CalendarEventResponse> events) {
        return events.stream()
                .mapToLong(event -> Duration.between(event.startTime(), event.endTime()).toMinutes())
                .filter(duration -> duration > 0)
                .sum();
    }

    private Map<String, Long> buildCategoryStats(List<CalendarEventResponse> events) {
        return events.stream()
                .collect(Collectors.groupingBy(
                        event -> resolveCategory(event).name(),
                        Collectors.counting()
                ));
    }

    private String buildSummary(
            LocalDate date,
            List<CalendarEventResponse> events,
            long busyMinutes,
            Map<String, Long> categoryStats
    ) {
        if (events.isEmpty()) {
            return date + " 没有已安排日程，你今天的时间比较开放。";
        }
        CalendarEventResponse firstEvent = events.get(0);
        String topCategory = categoryStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(EventCategory.OTHER.name());
        return date + " 共有 " + events.size() + " 个日程，已占用约 " + busyMinutes
                + " 分钟。主要安排集中在 " + topCategory + " 分类，第一个日程是「"
                + firstEvent.title() + "」，从 " + firstEvent.startTime().toLocalTime() + " 开始。";
    }

    private EventCategory resolveCategory(CalendarEventResponse event) {
        return event.category() == null ? EventCategory.OTHER : event.category();
    }
}
