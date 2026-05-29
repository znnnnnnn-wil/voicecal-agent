package com.voicecal.modules.ai.tool;

import com.voicecal.common.exception.ResourceNotFoundException;
import com.voicecal.modules.calendar.entity.request.CalendarEventCreateRequest;
import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import com.voicecal.modules.calendar.service.CalendarEventService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 日程工具集合，供 LangChain4j Tool Calling 调用。
 */
@Component
public class CalendarEventTools {

    private final CalendarEventService calendarEventService;

    public CalendarEventTools(CalendarEventService calendarEventService) {
        this.calendarEventService = calendarEventService;
    }

    /**
     * 查询所有有效日程。
     *
     * @return 日程列表文本
     */
    @Tool("List all calendar events ordered by start time ascending.")
    public String listCalendarEvents() {
        List<CalendarEventResponse> events = calendarEventService.listEvents();
        if (events.isEmpty()) {
            return "当前没有日程。";
        }
        return formatEvents(events);
    }

    /**
     * 根据 ID 查询单个日程。
     *
     * @param id 日程 ID
     * @return 日程详情文本
     */
    @Tool("Get one calendar event by id.")
    public String getCalendarEventById(@P(name = "id", description = "Calendar event id") Long id) {
        try {
            return formatEvent(calendarEventService.getEvent(id));
        } catch (ResourceNotFoundException exception) {
            return "查询日程失败：" + exception.getMessage();
        } catch (IllegalArgumentException exception) {
            return "查询日程失败：" + exception.getMessage();
        }
    }

    /**
     * 创建日程事件。
     *
     * @param title 日程标题
     * @param description 日程描述
     * @param startTime 开始时间，ISO-8601 LocalDateTime 字符串
     * @param endTime 结束时间，ISO-8601 LocalDateTime 字符串
     * @param location 地点
     * @return 创建结果文本
     */
    @Tool("Create a calendar event.")
    public String createCalendarEvent(
            @P(name = "title", description = "Calendar event title") String title,
            @P(name = "description", description = "Calendar event description", required = false) String description,
            @P(name = "startTime", description = "Start time in ISO-8601 LocalDateTime format") String startTime,
            @P(name = "endTime", description = "End time in ISO-8601 LocalDateTime format") String endTime,
            @P(name = "location", description = "Calendar event location", required = false) String location
    ) {
        try {
            CalendarEventResponse event = calendarEventService.createEvent(new CalendarEventCreateRequest(
                    title,
                    normalizeBlank(description),
                    parseDateTime(startTime),
                    parseDateTime(endTime),
                    normalizeBlank(location)
            ));
            return "创建日程成功：" + formatEvent(event);
        } catch (DateTimeParseException exception) {
            return "创建日程失败：时间格式不正确，请使用 ISO-8601 LocalDateTime，例如 2026-06-01T10:00:00";
        } catch (ConstraintViolationException exception) {
            return "创建日程失败：" + formatConstraintViolations(exception);
        } catch (IllegalArgumentException exception) {
            return "创建日程失败：" + exception.getMessage();
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("开始时间和结束时间不能为空");
        }
        return LocalDateTime.parse(value);
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private String formatEvents(List<CalendarEventResponse> events) {
        return events.stream()
                .map(this::formatEvent)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("当前没有日程。");
    }

    private String formatEvent(CalendarEventResponse event) {
        StringBuilder builder = new StringBuilder();
        builder.append("ID: ").append(event.id())
                .append(", 标题: ").append(event.title())
                .append(", 开始: ").append(event.startTime())
                .append(", 结束: ").append(event.endTime());
        if (event.location() != null && !event.location().isBlank()) {
            builder.append(", 地点: ").append(event.location());
        }
        if (event.description() != null && !event.description().isBlank()) {
            builder.append(", 描述: ").append(event.description());
        }
        return builder.toString();
    }

    private String formatConstraintViolations(ConstraintViolationException exception) {
        return exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("；"));
    }
}
