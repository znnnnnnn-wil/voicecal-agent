package com.voicecal.modules.calendar.service;

import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import com.voicecal.common.enums.dao.EventCategory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 日历事件查询服务接口，负责按日期和时间范围查询日程。
 */
public interface CalendarEventQueryService {

    /**
     * 查询指定时区下今天的日程。
     *
     * @param timezone 时区 ID，可为空
     * @return 今天的日程列表
     */
    List<CalendarEventResponse> getTodayEvents(String timezone);

    /**
     * 查询指定时区下本周的日程。
     *
     * @param timezone 时区 ID，可为空
     * @return 本周的日程列表
     */
    List<CalendarEventResponse> getCurrentWeekEvents(String timezone);

    /**
     * 查询指定日期内的日程。
     *
     * @param date 日期字符串，格式 yyyy-MM-dd
     * @param timezone 时区 ID，可为空
     * @return 指定日期内的日程列表
     */
    List<CalendarEventResponse> getEventsForDate(String date, String timezone);

    /**
     * 查询指定日期内的日程。
     *
     * @param date 日期
     * @param zoneId 时区
     * @return 指定日期内的日程列表
     */
    List<CalendarEventResponse> getEventsForDate(LocalDate date, ZoneId zoneId);

    /**
     * 按时间范围、关键词和分类搜索日程。
     *
     * @param rangeStart 查询范围开始时间
     * @param rangeEnd 查询范围结束时间；等于 rangeStart 时表示精确时间点查询
     * @param keyword 标题或描述关键词，可为空
     * @param category 日程分类，可为空
     * @return 匹配条件的日程列表
     */
    List<CalendarEventResponse> searchEvents(
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            String keyword,
            EventCategory category
    );

    /**
     * 解析时区参数。
     *
     * @param timezone 时区 ID，可为空
     * @return 解析后的时区
     */
    ZoneId resolveZoneId(String timezone);

    /**
     * 解析日期参数，缺省时返回指定时区下的今天。
     *
     * @param date 日期字符串，可为空
     * @param zoneId 时区
     * @return 解析后的日期
     */
    LocalDate resolveDate(String date, ZoneId zoneId);
}
