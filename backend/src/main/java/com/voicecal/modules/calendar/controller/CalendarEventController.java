package com.voicecal.modules.calendar.controller;

import com.voicecal.common.response.ApiResponse;
import com.voicecal.modules.calendar.entity.request.CalendarEventCreateRequest;
import com.voicecal.modules.calendar.entity.request.CalendarEventUpdateRequest;
import com.voicecal.modules.calendar.entity.request.ConflictCheckRequest;
import com.voicecal.modules.calendar.entity.request.FreeTimeQueryRequest;
import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import com.voicecal.modules.calendar.entity.response.ConflictCheckResponse;
import com.voicecal.modules.calendar.entity.response.FreeTimeSlotResponse;
import com.voicecal.modules.calendar.service.CalendarAvailabilityService;
import com.voicecal.modules.calendar.service.CalendarEventQueryService;
import com.voicecal.modules.calendar.service.CalendarEventService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日历事件接口控制器，提供基础 CRUD API。
 */
@RestController
@RequestMapping("/api/calendar/events")
public class CalendarEventController {

    private final CalendarEventService calendarEventService;
    private final CalendarEventQueryService calendarEventQueryService;
    private final CalendarAvailabilityService calendarAvailabilityService;

    public CalendarEventController(
            CalendarEventService calendarEventService,
            CalendarEventQueryService calendarEventQueryService,
            CalendarAvailabilityService calendarAvailabilityService
    ) {
        this.calendarEventService = calendarEventService;
        this.calendarEventQueryService = calendarEventQueryService;
        this.calendarAvailabilityService = calendarAvailabilityService;
    }

    /**
     * 创建日历事件。
     *
     * @param request 创建请求
     * @return 创建后的日历事件
     */
    @PostMapping
    public ApiResponse<CalendarEventResponse> createEvent(@Valid @RequestBody CalendarEventCreateRequest request) {
        return ApiResponse.success("创建日历事件成功", calendarEventService.createEvent(request));
    }

    /**
     * 查询日历事件列表。
     *
     * @return 有效日历事件列表
     */
    @GetMapping
    public ApiResponse<List<CalendarEventResponse>> listEvents() {
        return ApiResponse.success("查询日历事件列表成功", calendarEventService.listEvents());
    }

    /**
     * 检测指定时间段是否与已有日程冲突。
     *
     * @param request 冲突检测请求
     * @return 冲突检测结果
     */
    @PostMapping("/conflicts")
    public ApiResponse<ConflictCheckResponse> checkConflicts(@Valid @RequestBody ConflictCheckRequest request) {
        return ApiResponse.success("日程冲突检测成功", calendarAvailabilityService.checkConflicts(request));
    }

    /**
     * 查询指定时间范围内的空闲时间段。
     *
     * @param startTime 查询开始时间
     * @param endTime 查询结束时间
     * @param minMinutes 最小空闲分钟数，可为空
     * @return 空闲时间段列表
     */
    @GetMapping("/free-time")
    public ApiResponse<List<FreeTimeSlotResponse>> findFreeTimeSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Integer minMinutes
    ) {
        return ApiResponse.success(
                "查询空闲时间成功",
                calendarAvailabilityService.findFreeTimeSlots(new FreeTimeQueryRequest(startTime, endTime, minMinutes))
        );
    }

    /**
     * 查询指定时区下今天的日历事件。
     *
     * @param timezone 时区 ID，可为空
     * @return 今天的日历事件列表
     */
    @GetMapping("/today")
    public ApiResponse<List<CalendarEventResponse>> listTodayEvents(
            @RequestParam(required = false) String timezone
    ) {
        return ApiResponse.success("查询今日日程成功", calendarEventQueryService.getTodayEvents(timezone));
    }

    /**
     * 查询指定时区下本周的日历事件。
     *
     * @param timezone 时区 ID，可为空
     * @return 本周的日历事件列表
     */
    @GetMapping("/week")
    public ApiResponse<List<CalendarEventResponse>> listWeekEvents(
            @RequestParam(required = false) String timezone
    ) {
        return ApiResponse.success("查询本周日程成功", calendarEventQueryService.getCurrentWeekEvents(timezone));
    }

    /**
     * 查询指定日期内的日历事件。
     *
     * @param date 日期字符串，格式 yyyy-MM-dd
     * @param timezone 时区 ID，可为空
     * @return 指定日期的日历事件列表
     */
    @GetMapping("/by-date")
    public ApiResponse<List<CalendarEventResponse>> listEventsByDate(
            @RequestParam String date,
            @RequestParam(required = false) String timezone
    ) {
        return ApiResponse.success("查询指定日期日程成功", calendarEventQueryService.getEventsForDate(date, timezone));
    }

    /**
     * 查询日历事件详情。
     *
     * @param id 日程 ID
     * @return 日历事件详情
     */
    @GetMapping("/{id}")
    public ApiResponse<CalendarEventResponse> getEvent(@PathVariable Long id) {
        return ApiResponse.success("查询日历事件成功", calendarEventService.getEvent(id));
    }

    /**
     * 更新日历事件。
     *
     * @param id 日程 ID
     * @param request 更新请求
     * @return 更新后的日历事件
     */
    @PutMapping("/{id}")
    public ApiResponse<CalendarEventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody CalendarEventUpdateRequest request
    ) {
        return ApiResponse.success("更新日历事件成功", calendarEventService.updateEvent(id, request));
    }

    /**
     * 删除日历事件。
     *
     * @param id 日程 ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteEvent(@PathVariable Long id) {
        calendarEventService.deleteEvent(id);
        return ApiResponse.success("删除日历事件成功", null);
    }
}
