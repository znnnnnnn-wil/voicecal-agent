package com.voicecal.modules.ai.tool;

import com.voicecal.common.enums.ResultCodeEnum;
import com.voicecal.common.exception.CustomException;
import com.voicecal.common.exception.ResourceNotFoundException;
import com.voicecal.modules.assistant.pending.PendingActionResponse;
import com.voicecal.modules.assistant.pending.PendingActionService;
import com.voicecal.modules.calendar.entity.request.CalendarEventCreateRequest;
import com.voicecal.modules.calendar.entity.request.CalendarEventUpdateRequest;
import com.voicecal.modules.calendar.entity.request.ConflictCheckRequest;
import com.voicecal.modules.calendar.entity.request.FreeTimeQueryRequest;
import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import com.voicecal.modules.calendar.entity.response.ConflictCheckResponse;
import com.voicecal.modules.calendar.entity.response.ConflictEventResponse;
import com.voicecal.modules.calendar.entity.response.FreeTimeSlotResponse;
import com.voicecal.modules.calendar.service.CalendarAvailabilityService;
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
    private final CalendarAvailabilityService calendarAvailabilityService;
    private final PendingActionService pendingActionService;

    public CalendarEventTools(
            CalendarEventService calendarEventService,
            CalendarAvailabilityService calendarAvailabilityService,
            PendingActionService pendingActionService
    ) {
        this.calendarEventService = calendarEventService;
        this.calendarAvailabilityService = calendarAvailabilityService;
        this.pendingActionService = pendingActionService;
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
        } catch (CustomException exception) {
            return "创建日程失败：" + exception.getMessage();
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
     * 创建待确认删除日程操作，不直接删除日程。
     *
     * @param conversationId 对话 ID
     * @param eventId 日程 ID
     * @return 待确认删除操作文本
     */
    @Tool("Create a pending delete action for a calendar event. This tool must not delete the event directly.")
    public String createPendingDeleteAction(
            @P(name = "conversationId", description = "Conversation id", required = false) String conversationId,
            @P(name = "eventId", description = "Calendar event id to delete") Long eventId
    ) {
        try {
            PendingActionResponse action = pendingActionService.createPendingDeleteAction(conversationId, eventId);
            return "已创建待确认删除操作，请确认后再删除。\n"
                    + "操作 ID: " + action.id() + "\n"
                    + "目标: " + action.targetSummary() + "\n"
                    + "过期时间: " + action.expiresAt();
        } catch (CustomException exception) {
            return "创建待确认删除操作失败：" + exception.getMessage();
        }
    }

    /**
     * 创建待确认更新日程操作，不直接修改日程。
     *
     * @param conversationId 对话 ID
     * @param eventId 日程 ID
     * @param title 更新后的标题
     * @param description 更新后的描述
     * @param startTime 更新后的开始时间，ISO-8601 LocalDateTime 字符串
     * @param endTime 更新后的结束时间，ISO-8601 LocalDateTime 字符串
     * @param location 更新后的地点
     * @return 待确认更新操作文本
     */
    @Tool("Create a pending update action for a calendar event. This tool must not update the event directly.")
    public String createPendingUpdateAction(
            @P(name = "conversationId", description = "Conversation id", required = false) String conversationId,
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
            PendingActionResponse action = pendingActionService.createPendingUpdateAction(
                    conversationId,
                    eventId,
                    new CalendarEventUpdateRequest(
                            title,
                            normalizeBlank(description),
                            parseDateTime(startTime),
                            parseDateTime(endTime),
                            normalizeBlank(location)
                    )
            );
            return "已创建待确认更新操作，请确认后再修改。\n"
                    + "操作 ID: " + action.id() + "\n"
                    + "目标: " + action.targetSummary() + "\n"
                    + "过期时间: " + action.expiresAt();
        } catch (DateTimeParseException exception) {
            return "创建待确认更新操作失败：时间格式不正确，请使用 ISO-8601 LocalDateTime，例如 2026-06-01T10:00:00";
        } catch (CustomException exception) {
            return "创建待确认更新操作失败：" + exception.getMessage();
        }
    }

    /**
     * 确认并执行待确认操作。
     *
     * @param conversationId 对话 ID
     * @param actionId 操作 ID
     * @return 执行结果文本
     */
    @Tool("Confirm and execute a pending action by id.")
    public String confirmPendingAction(
            @P(name = "conversationId", description = "Conversation id", required = false) String conversationId,
            @P(name = "actionId", description = "Pending action id") String actionId
    ) {
        try {
            return pendingActionService.confirmPendingAction(conversationId, actionId);
        } catch (CustomException exception) {
            return "确认待确认操作失败：" + exception.getMessage();
        }
    }

    /**
     * 取消待确认操作。
     *
     * @param conversationId 对话 ID
     * @param actionId 操作 ID
     * @return 取消结果文本
     */
    @Tool("Cancel a pending action by id.")
    public String cancelPendingAction(
            @P(name = "conversationId", description = "Conversation id", required = false) String conversationId,
            @P(name = "actionId", description = "Pending action id") String actionId
    ) {
        try {
            return pendingActionService.cancelPendingAction(conversationId, actionId);
        } catch (CustomException exception) {
            return "取消待确认操作失败：" + exception.getMessage();
        }
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

    private String formatConflictEvents(List<ConflictEventResponse> events) {
        return events.stream()
                .map(event -> {
                    StringBuilder builder = new StringBuilder();
                    builder.append("ID: ").append(event.id())
                            .append(", 标题: ").append(event.title())
                            .append(", 开始: ").append(event.startTime())
                            .append(", 结束: ").append(event.endTime());
                    if (event.location() != null && !event.location().isBlank()) {
                        builder.append(", 地点: ").append(event.location());
                    }
                    return builder.toString();
                })
                .collect(Collectors.joining("\n"));
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
}
