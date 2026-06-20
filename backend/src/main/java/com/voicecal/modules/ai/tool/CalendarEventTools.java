package com.voicecal.modules.ai.tool;

import com.voicecal.common.enums.ResultCodeEnum;
import com.voicecal.common.enums.dao.EventCategory;
import com.voicecal.common.exception.CustomException;
import com.voicecal.modules.ai.context.AiRequestContext;
import com.voicecal.modules.calendar.entity.request.CalendarEventCreateRequest;
import com.voicecal.modules.calendar.entity.request.CalendarEventUpdateRequest;
import com.voicecal.modules.calendar.entity.request.ConflictCheckRequest;
import com.voicecal.modules.calendar.entity.request.FreeTimeQueryRequest;
import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import com.voicecal.modules.calendar.entity.response.ConflictCheckResponse;
import com.voicecal.modules.calendar.entity.response.ConflictEventResponse;
import com.voicecal.modules.calendar.entity.response.FreeTimeSlotResponse;
import com.voicecal.modules.calendar.service.CalendarAvailabilityService;
import com.voicecal.modules.calendar.service.CalendarEventQueryService;
import com.voicecal.modules.calendar.service.CalendarEventService;
import com.voicecal.modules.calendar.service.IcsExportService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private final CalendarEventQueryService calendarEventQueryService;
    private final CalendarAvailabilityService calendarAvailabilityService;
    private final IcsExportService icsExportService;

    public CalendarEventTools(
            CalendarEventService calendarEventService,
            CalendarEventQueryService calendarEventQueryService,
            CalendarAvailabilityService calendarAvailabilityService,
            IcsExportService icsExportService
    ) {
        this.calendarEventService = calendarEventService;
        this.calendarEventQueryService = calendarEventQueryService;
        this.calendarAvailabilityService = calendarAvailabilityService;
        this.icsExportService = icsExportService;
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
        } catch (CustomException exception) {
            return "查询日程失败：" + exception.getMessage();
        }
    }

    /**
     * 按时间范围、关键词和分类搜索日程。
     *
     * @param startTime 查询开始时间，ISO-8601 LocalDateTime 字符串
     * @param endTime 查询结束时间，ISO-8601 LocalDateTime 字符串
     * @param keyword 标题或描述关键词，可为空
     * @param category 日程分类，可为空
     * @return 匹配日程文本
     */
    @Tool("Search calendar events by time range, optional keyword, and optional category. Use this before getCalendarEventById when the user identifies an event by date, time, title, or type instead of id. For an exact time, pass the same ISO-8601 LocalDateTime as startTime and endTime.")
    public String searchCalendarEvents(
            @P(name = "startTime", description = "Search range start time in ISO-8601 LocalDateTime format") String startTime,
            @P(name = "endTime", description = "Search range end time in ISO-8601 LocalDateTime format. For an exact time, use the same value as startTime.") String endTime,
            @P(name = "keyword", description = "Optional title or description keyword from the user's request", required = false) String keyword,
            @P(name = "category", description = "Optional calendar event category: WORK, STUDY, LIFE, MEETING, INTERVIEW, OTHER. Use MEETING when the user asks for a meeting.", required = false) String category
    ) {
        try {
            List<CalendarEventResponse> events = calendarEventQueryService.searchEvents(
                    parseDateTime(startTime),
                    parseDateTime(endTime),
                    normalizeBlank(keyword),
                    parseCategory(category)
            );
            if (events.isEmpty()) {
                return "没有找到匹配的日程。";
            }
            return "找到匹配日程：\n" + formatEvents(events);
        } catch (DateTimeParseException exception) {
            return "查询日程失败：时间格式不正确，请使用 ISO-8601 LocalDateTime，例如 2026-06-01T10:00:00";
        } catch (CustomException exception) {
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
     * @param category 日程分类，可为空
     * @param reminderMinutes 提前提醒分钟数，可为空
     * @return 创建结果文本
     */
    @Tool("Create a calendar event. If the user does not specify duration or end time, use the same ISO-8601 LocalDateTime value for startTime and endTime.")
    public String createCalendarEvent(
            @P(name = "title", description = "Calendar event title") String title,
            @P(name = "description", description = "Calendar event description", required = false) String description,
            @P(name = "startTime", description = "Start time in ISO-8601 LocalDateTime format") String startTime,
            @P(name = "endTime", description = "End time in ISO-8601 LocalDateTime format. If no duration is specified, use the same value as startTime.") String endTime,
            @P(name = "location", description = "Calendar event location", required = false) String location,
            @P(name = "category", description = "Calendar event category: WORK, STUDY, LIFE, MEETING, INTERVIEW, OTHER", required = false)
            String category,
            @P(name = "reminderMinutes", description = "Reminder minutes before start time. Use 0 for due-time reminders.", required = false)
            Integer reminderMinutes
    ) {
        try {
            CalendarEventResponse event = calendarEventService.createEvent(new CalendarEventCreateRequest(
                    title,
                    normalizeBlank(description),
                    parseDateTime(startTime),
                    parseDateTime(endTime),
                    normalizeBlank(location),
                    reminderMinutes,
                    parseCategory(category)
            ));
            return "创建日程成功：" + formatEvent(event);
        } catch (DateTimeParseException exception) {
            return "创建日程失败：时间格式不正确，请使用 ISO-8601 LocalDateTime，例如 2026-06-01T10:00:00";
        } catch (ConstraintViolationException exception) {
            return "创建日程失败：" + formatConstraintViolations(exception);
        } catch (CustomException exception) {
            return "success=false, message=" + exception.getMessage();
        }
    }

    /**
     * 检测指定时间段是否与已有日程冲突。
     *
     * @param startTime 开始时间，ISO-8601 LocalDateTime 字符串
     * @param endTime 结束时间，ISO-8601 LocalDateTime 字符串
     * @param excludeEventId 需要排除的日程 ID，可为空
     * @return 冲突检测结果文本
     */
    @Tool("Check whether a time range conflicts with existing calendar events.")
    public String checkCalendarConflict(
            @P(name = "startTime", description = "Start time in ISO-8601 LocalDateTime format") String startTime,
            @P(name = "endTime", description = "End time in ISO-8601 LocalDateTime format") String endTime,
            @P(name = "excludeEventId", description = "Calendar event id to exclude", required = false)
            Long excludeEventId
    ) {
        try {
            ConflictCheckResponse response = calendarAvailabilityService.checkConflicts(new ConflictCheckRequest(
                    parseDateTime(startTime),
                    parseDateTime(endTime),
                    excludeEventId
            ));
            if (!response.hasConflict()) {
                return "该时间段没有日程冲突。";
            }
            return "该时间段存在日程冲突：\n" + formatConflictEvents(response.conflicts());
        } catch (DateTimeParseException exception) {
            return "冲突检测失败：时间格式不正确，请使用 ISO-8601 LocalDateTime，例如 2026-06-01T10:00:00";
        } catch (CustomException exception) {
            return "冲突检测失败：" + exception.getMessage();
        }
    }

    /**
     * 查询指定时间范围内的空闲时间段。
     *
     * @param startTime 查询开始时间，ISO-8601 LocalDateTime 字符串
     * @param endTime 查询结束时间，ISO-8601 LocalDateTime 字符串
     * @param minMinutes 最小空闲分钟数，可为空
     * @return 空闲时间段文本
     */
    @Tool("Find available free time slots in a calendar time range.")
    public String findFreeTime(
            @P(name = "startTime", description = "Start time in ISO-8601 LocalDateTime format") String startTime,
            @P(name = "endTime", description = "End time in ISO-8601 LocalDateTime format") String endTime,
            @P(name = "minMinutes", description = "Minimum free slot minutes", required = false) Integer minMinutes
    ) {
        try {
            List<FreeTimeSlotResponse> slots = calendarAvailabilityService.findFreeTimeSlots(new FreeTimeQueryRequest(
                    parseDateTime(startTime),
                    parseDateTime(endTime),
                    minMinutes
            ));
            if (slots.isEmpty()) {
                return "该时间范围内没有满足条件的空闲时间。";
            }
            return "可用空闲时间：\n" + formatFreeTimeSlots(slots);
        } catch (DateTimeParseException exception) {
            return "查询空闲时间失败：时间格式不正确，请使用 ISO-8601 LocalDateTime，例如 2026-06-01T10:00:00";
        } catch (CustomException exception) {
            return "查询空闲时间失败：" + exception.getMessage();
        }
    }

    /**
     * 生成指定时间范围内日程的 ICS 下载链接。
     *
     * @param startTime 导出开始时间，ISO-8601 LocalDateTime 字符串
     * @param endTime 导出结束时间，ISO-8601 LocalDateTime 字符串
     * @return ICS 下载链接文本
     */
    @Tool("Generate an ICS download link for calendar events in a time range.")
    public String exportCalendarEventsIcs(
            @P(name = "startTime", description = "Export range start time in ISO-8601 LocalDateTime format") String startTime,
            @P(name = "endTime", description = "Export range end time in ISO-8601 LocalDateTime format") String endTime
    ) {
        try {
            LocalDateTime parsedStartTime = parseDateTime(startTime);
            LocalDateTime parsedEndTime = parseDateTime(endTime);
            icsExportService.exportEvents(parsedStartTime, parsedEndTime);
            String downloadUrl = "/api/calendar/events/ics?startTime="
                    + urlEncode(parsedStartTime.toString())
                    + "&endTime="
                    + urlEncode(parsedEndTime.toString());
            return "ICS 导出链接已生成：" + downloadUrl;
        } catch (DateTimeParseException exception) {
            return "生成 ICS 导出链接失败：时间格式不正确，请使用 ISO-8601 LocalDateTime，例如 2026-06-01T10:00:00";
        } catch (CustomException exception) {
            return "生成 ICS 导出链接失败：" + exception.getMessage();
        }
    }

    /**
     * 直接删除日程。
     *
     * @param eventId 日程 ID
     * @return 删除结果文本
     */
    @Tool("Delete a calendar event by id only after the user has explicitly confirmed the deletion.")
    public String deleteCalendarEvent(
            @P(name = "eventId", description = "Calendar event id to delete") Long eventId
    ) {
        try {
            CalendarEventResponse event = calendarEventService.getEvent(eventId);
            if (isMeetingDeleteRequest() && !isMeetingEvent(event)) {
                return "删除操作已拦截：用户要求删除会议，但目标日程不是会议。请补充会议标题或更精确的时间。";
            }
            if (needsImportantOperationConfirmation()) {
                return "请确认是否删除日程：" + formatEvent(event) + "。回复“确认”后我再执行删除。";
            }
            calendarEventService.deleteEvent(eventId);
            return "删除日程成功：" + event.title();
        } catch (CustomException exception) {
            return "删除日程失败：" + exception.getMessage();
        }
    }

    /**
     * 直接更新日程。
     *
     * @param eventId 日程 ID
     * @param title 更新后的标题
     * @param description 更新后的描述
     * @param startTime 更新后的开始时间，ISO-8601 LocalDateTime 字符串
     * @param endTime 更新后的结束时间，ISO-8601 LocalDateTime 字符串
     * @param location 更新后的地点
     * @return 更新结果文本
     */
    @Tool("Update a calendar event by id only after the user has explicitly confirmed the update.")
    public String updateCalendarEvent(
            @P(name = "eventId", description = "Calendar event id to update") Long eventId,
            @P(name = "title", description = "Updated calendar event title") String title,
            @P(name = "description", description = "Updated calendar event description", required = false)
            String description,
            @P(name = "startTime", description = "Updated start time in ISO-8601 LocalDateTime format")
            String startTime,
            @P(name = "endTime", description = "Updated end time in ISO-8601 LocalDateTime format") String endTime,
            @P(name = "location", description = "Updated calendar event location", required = false) String location
    ) {
        try {
            CalendarEventResponse existingEvent = calendarEventService.getEvent(eventId);
            if (needsImportantOperationConfirmation()) {
                return "请确认是否修改日程：" + formatEvent(existingEvent)
                        + "。修改后标题：" + title
                        + "，开始：" + startTime
                        + "，结束：" + endTime
                        + (normalizeBlank(location) == null ? "" : "，地点：" + location)
                        + "。回复“确认”后我再执行修改。";
            }
            CalendarEventResponse event = calendarEventService.updateEvent(
                    eventId,
                    new CalendarEventUpdateRequest(
                            title,
                            normalizeBlank(description),
                            parseDateTime(startTime),
                            parseDateTime(endTime),
                            normalizeBlank(location),
                            null,
                            null
                    )
            );
            return "更新日程成功：" + formatEvent(event);
        } catch (DateTimeParseException exception) {
            return "更新日程失败：时间格式不正确，请使用 ISO-8601 LocalDateTime，例如 2026-06-01T10:00:00";
        } catch (CustomException exception) {
            return "更新日程失败：" + exception.getMessage();
        }
    }

    private boolean isMeetingDeleteRequest() {
        String message = normalizeContextMessage();
        return message.contains("删除") && (message.contains("会议") || message.contains("meeting"));
    }

    private boolean needsImportantOperationConfirmation() {
        String message = normalizeContextMessage()
                .replace(" ", "")
                .replace("，", "")
                .replace(",", "")
                .replace("。", "")
                .replace("！", "")
                .replace("!", "");
        if (message.isBlank()) {
            return false;
        }
        if (message.contains("确认删除") || message.contains("确定删除") || message.contains("执行删除")) {
            return false;
        }
        if (message.contains("确认修改") || message.contains("确定修改") || message.contains("执行修改")) {
            return false;
        }
        if (message.contains("删除吧") || message.contains("删吧") || message.contains("修改吧") || message.contains("改吧")) {
            return false;
        }
        return message.length() > 8 || !containsAny(message, "确认", "确定", "是的", "对", "同意", "执行", "没问题");
    }

    private boolean isMeetingEvent(CalendarEventResponse event) {
        if (event.category() == EventCategory.MEETING) {
            return true;
        }
        String text = ((event.title() == null ? "" : event.title())
                + " "
                + (event.description() == null ? "" : event.description())).toLowerCase();
        return text.contains("会议")
                || text.contains("开会")
                || text.contains("例会")
                || text.contains("晨会")
                || text.contains("早会")
                || text.contains("meeting")
                || text.contains("review")
                || text.contains("sync")
                || text.contains("standup");
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeContextMessage() {
        String message = AiRequestContext.getUserMessage();
        return message == null ? "" : message.trim().toLowerCase();
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            throw CustomException.create(ResultCodeEnum.PARAMS_ERROR, "开始时间和结束时间不能为空");
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
        StringBuilder builder = appendBaseEventFields(
                new StringBuilder(),
                event.id(),
                event.title(),
                event.startTime(),
                event.endTime(),
                event.location()
        );
        builder.append(", 分类: ").append(event.category());
        if (event.description() != null && !event.description().isBlank()) {
            builder.append(", 描述: ").append(event.description());
        }
        return builder.toString();
    }

    private EventCategory parseCategory(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return EventCategory.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw CustomException.create(ResultCodeEnum.PARAMS_ERROR, "日程分类不正确，请使用 WORK、STUDY、LIFE、MEETING、INTERVIEW 或 OTHER");
        }
    }

    private String formatConflictEvents(List<ConflictEventResponse> events) {
        return events.stream()
                .map(this::formatConflictEvent)
                .collect(Collectors.joining("\n"));
    }

    private String formatConflictEvent(ConflictEventResponse event) {
        return appendBaseEventFields(
                new StringBuilder(),
                event.id(),
                event.title(),
                event.startTime(),
                event.endTime(),
                event.location()
        ).toString();
    }

    private StringBuilder appendBaseEventFields(
            StringBuilder builder,
            Long id,
            String title,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String location
    ) {
        builder.append("ID: ").append(id)
                .append(", 标题: ").append(title)
                .append(", 开始: ").append(startTime)
                .append(", 结束: ").append(endTime);
        if (location != null && !location.isBlank()) {
            builder.append(", 地点: ").append(location);
        }
        return builder;
    }

    private String formatFreeTimeSlots(List<FreeTimeSlotResponse> slots) {
        return slots.stream()
                .map(slot -> "开始: " + slot.startTime()
                        + ", 结束: " + slot.endTime()
                        + ", 时长: " + slot.minutes() + " 分钟")
                .collect(Collectors.joining("\n"));
    }

    private String formatConstraintViolations(ConstraintViolationException exception) {
        return exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("；"));
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
