package com.voicecal.modules.calendar.service.impl;

import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.common.exception.ResourceNotFoundException;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import com.voicecal.modules.calendar.entity.request.CalendarEventCreateRequest;
import com.voicecal.modules.calendar.entity.request.CalendarEventUpdateRequest;
import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import com.voicecal.modules.calendar.service.CalendarEventService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 日历事件服务实现，负责基础 CRUD 业务规则和数据持久化。
 */
@Service
public class CalendarEventServiceImpl implements CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;

    public CalendarEventServiceImpl(CalendarEventRepository calendarEventRepository) {
        this.calendarEventRepository = calendarEventRepository;
    }

    /**
     * 创建日历事件。
     *
     * @param request 创建请求
     * @return 创建后的日历事件
     */
    @Override
    @Transactional
    public CalendarEventResponse createEvent(CalendarEventCreateRequest request) {
        validateTimeRange(request.startTime(), request.endTime());

        CalendarEvent event = new CalendarEvent();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setLocation(request.location());
        event.setStatus(EventStatus.ACTIVE);

        return CalendarEventResponse.from(calendarEventRepository.save(event));
    }

    /**
     * 查询有效日历事件列表。
     *
     * @return 按开始时间升序排列的日历事件列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> listEvents() {
        return calendarEventRepository.findByStatusOrderByStartTimeAsc(EventStatus.ACTIVE)
                .stream()
                .map(CalendarEventResponse::from)
                .toList();
    }

    /**
     * 根据 ID 查询有效日历事件。
     *
     * @param id 日程 ID
     * @return 日历事件
     */
    @Override
    @Transactional(readOnly = true)
    public CalendarEventResponse getEvent(Long id) {
        return CalendarEventResponse.from(findActiveEvent(id));
    }

    /**
     * 更新日历事件。
     *
     * @param id 日程 ID
     * @param request 更新请求
     * @return 更新后的日历事件
     */
    @Override
    @Transactional
    public CalendarEventResponse updateEvent(Long id, CalendarEventUpdateRequest request) {
        validateTimeRange(request.startTime(), request.endTime());

        CalendarEvent event = findActiveEvent(id);
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setLocation(request.location());

        return CalendarEventResponse.from(event);
    }

    /**
     * 删除日历事件。
     *
     * @param id 日程 ID
     */
    @Override
    @Transactional
    public void deleteEvent(Long id) {
        CalendarEvent event = findActiveEvent(id);
        calendarEventRepository.delete(event);
    }

    private CalendarEvent findActiveEvent(Long id) {
        return calendarEventRepository.findByIdAndStatus(id, EventStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("日历事件不存在"));
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return;
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间");
        }
    }
}
