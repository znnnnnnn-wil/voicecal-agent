package com.voicecal.modules.calendar.service.impl;

import com.voicecal.common.enums.ResultCodeEnum;
import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.common.exception.CustomException;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import com.voicecal.modules.calendar.service.CalendarEventQueryService;
import java.time.DayOfWeek;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 日历事件查询服务实现，统一处理日期、时区和时间范围重叠查询。
 */
@Service
public class CalendarEventQueryServiceImpl implements CalendarEventQueryService {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of(CalendarEvent.DEFAULT_TIMEZONE);

    private final CalendarEventRepository calendarEventRepository;

    public CalendarEventQueryServiceImpl(CalendarEventRepository calendarEventRepository) {
        this.calendarEventRepository = calendarEventRepository;
    }

    /**
     * 查询指定时区下今天的日程。
     *
     * @param timezone 时区 ID，可为空
     * @return 今天的日程列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getTodayEvents(String timezone) {
        ZoneId zoneId = resolveZoneId(timezone);
        return getEventsForDate(LocalDate.now(zoneId), zoneId);
    }

    /**
     * 查询指定时区下本周的日程。
     *
     * @param timezone 时区 ID，可为空
     * @return 本周的日程列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getCurrentWeekEvents(String timezone) {
        ZoneId zoneId = resolveZoneId(timezone);
        LocalDate today = LocalDate.now(zoneId);
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStart = weekStartDate.atStartOfDay();
        LocalDateTime weekEnd = weekStart.plusWeeks(1);
        return findOverlappingEvents(weekStart, weekEnd);
    }

    /**
     * 查询指定日期内的日程。
     *
     * @param date 日期字符串，格式 yyyy-MM-dd
     * @param timezone 时区 ID，可为空
     * @return 指定日期内的日程列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getEventsForDate(String date, String timezone) {
        ZoneId zoneId = resolveZoneId(timezone);
        return getEventsForDate(resolveDate(date, zoneId), zoneId);
    }

    /**
     * 查询指定日期内的日程。
     *
     * @param date 日期
     * @param zoneId 时区
     * @return 指定日期内的日程列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getEventsForDate(LocalDate date, ZoneId zoneId) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        return findOverlappingEvents(dayStart, dayEnd);
    }

    /**
     * 解析时区参数。
     *
     * @param timezone 时区 ID，可为空
     * @return 解析后的时区
     */
    @Override
    public ZoneId resolveZoneId(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return DEFAULT_ZONE_ID;
        }
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException exception) {
            throw CustomException.create(ResultCodeEnum.PARAMS_ERROR, "timezone 参数不是有效的 IANA timezone");
        }
    }

    /**
     * 解析日期参数，缺省时返回指定时区下的今天。
     *
     * @param date 日期字符串，可为空
     * @param zoneId 时区
     * @return 解析后的日期
     */
    @Override
    public LocalDate resolveDate(String date, ZoneId zoneId) {
        if (date == null || date.isBlank()) {
            return LocalDate.now(zoneId);
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException exception) {
            throw CustomException.create(ResultCodeEnum.PARAMS_ERROR, "date 参数格式必须为 yyyy-MM-dd");
        }
    }

    private List<CalendarEventResponse> findOverlappingEvents(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return calendarEventRepository.findOverlappingEvents(rangeStart, rangeEnd, EventStatus.ACTIVE)
                .stream()
                .map(CalendarEventResponse::from)
                .toList();
    }
}
