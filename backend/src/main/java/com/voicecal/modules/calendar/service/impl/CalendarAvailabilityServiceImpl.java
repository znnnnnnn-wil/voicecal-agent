package com.voicecal.modules.calendar.service.impl;

import com.voicecal.common.enums.ResultCodeEnum;
import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.common.exception.CustomException;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import com.voicecal.modules.calendar.entity.request.ConflictCheckRequest;
import com.voicecal.modules.calendar.entity.request.FreeTimeQueryRequest;
import com.voicecal.modules.calendar.entity.response.ConflictCheckResponse;
import com.voicecal.modules.calendar.entity.response.ConflictEventResponse;
import com.voicecal.modules.calendar.entity.response.FreeTimeSlotResponse;
import com.voicecal.modules.calendar.service.CalendarAvailabilityService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 日历可用性服务实现，负责冲突检测和空闲时间计算。
 */
@Service
public class CalendarAvailabilityServiceImpl implements CalendarAvailabilityService {

    private static final int DEFAULT_MIN_MINUTES = 30;

    private final CalendarEventRepository calendarEventRepository;

    public CalendarAvailabilityServiceImpl(CalendarEventRepository calendarEventRepository) {
        this.calendarEventRepository = calendarEventRepository;
    }

    /**
     * 检测指定时间段是否与已有日程冲突。
     *
     * @param request 冲突检测请求
     * @return 冲突检测结果
     */
    @Override
    @Transactional(readOnly = true)
    public ConflictCheckResponse checkConflicts(ConflictCheckRequest request) {
        validateTimeRange(request.startTime(), request.endTime());

        List<ConflictEventResponse> conflicts = calendarEventRepository.findOverlappingEventsExcludingEvent(
                        request.startTime(),
                        request.endTime(),
                        request.excludeEventId(),
                        EventStatus.ACTIVE
                )
                .stream()
                .map(ConflictEventResponse::from)
                .toList();

        return new ConflictCheckResponse(!conflicts.isEmpty(), conflicts);
    }

    /**
     * 查询指定时间范围内的空闲时间段。
     *
     * @param request 空闲时间查询请求
     * @return 空闲时间段列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<FreeTimeSlotResponse> findFreeTimeSlots(FreeTimeQueryRequest request) {
        return findFreeTimeSlots(request.startTime(), request.endTime(), request.minMinutes());
    }

    /**
     * 查询指定时间范围内的空闲时间段。
     *
     * @param startTime 查询开始时间
     * @param endTime 查询结束时间
     * @param minMinutes 最小空闲分钟数，可为空
     * @return 空闲时间段列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<FreeTimeSlotResponse> findFreeTimeSlots(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer minMinutes
    ) {
        validateTimeRange(startTime, endTime);
        int resolvedMinMinutes = resolveMinMinutes(minMinutes);

        List<CalendarEvent> busyEvents = calendarEventRepository.findOverlappingEvents(
                startTime,
                endTime,
                EventStatus.ACTIVE
        );
        List<FreeTimeSlotResponse> slots = new ArrayList<>();
        LocalDateTime cursor = startTime;

        for (CalendarEvent event : busyEvents) {
            LocalDateTime busyStart = max(event.getStartTime(), startTime);
            LocalDateTime busyEnd = min(event.getEndTime(), endTime);

            if (busyStart.isAfter(cursor)) {
                addSlotIfLongEnough(slots, cursor, busyStart, resolvedMinMinutes);
            }
            if (busyEnd.isAfter(cursor)) {
                cursor = busyEnd;
            }
        }

        if (cursor.isBefore(endTime)) {
            addSlotIfLongEnough(slots, cursor, endTime, resolvedMinMinutes);
        }

        return slots;
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw CustomException.create(ResultCodeEnum.PARAMS_ERROR, "开始时间和结束时间不能为空");
        }
        if (!endTime.isAfter(startTime)) {
            throw CustomException.create(ResultCodeEnum.PARAMS_ERROR, "结束时间必须晚于开始时间");
        }
    }

    private int resolveMinMinutes(Integer minMinutes) {
        int resolvedMinMinutes = minMinutes == null ? DEFAULT_MIN_MINUTES : minMinutes;
        if (resolvedMinMinutes <= 0) {
            throw CustomException.create(ResultCodeEnum.PARAMS_ERROR, "minMinutes 必须大于 0");
        }
        return resolvedMinMinutes;
    }

    private void addSlotIfLongEnough(
            List<FreeTimeSlotResponse> slots,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int minMinutes
    ) {
        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes >= minMinutes) {
            slots.add(new FreeTimeSlotResponse(startTime, endTime, minutes));
        }
    }

    private LocalDateTime max(LocalDateTime left, LocalDateTime right) {
        return left.isAfter(right) ? left : right;
    }

    private LocalDateTime min(LocalDateTime left, LocalDateTime right) {
        return left.isBefore(right) ? left : right;
    }
}
