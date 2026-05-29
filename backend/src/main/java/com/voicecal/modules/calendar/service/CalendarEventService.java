package com.voicecal.modules.calendar.service;

import com.voicecal.common.enums.dao.EventCategory;
import com.voicecal.modules.calendar.entity.request.CalendarEventCreateRequest;
import com.voicecal.modules.calendar.entity.request.CalendarEventUpdateRequest;
import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import java.util.List;

/**
 * 日历事件服务接口，定义日历事件基础 CRUD 能力。
 */
public interface CalendarEventService {

    /**
     * 创建日历事件。
     *
     * @param request 创建请求
     * @return 创建后的日历事件
     */
    CalendarEventResponse createEvent(CalendarEventCreateRequest request);

    /**
     * 查询有效日历事件列表。
     *
     * @return 按开始时间升序排列的日历事件列表
     */
    List<CalendarEventResponse> listEvents();

    /**
     * 查询有效日历事件列表，可按分类筛选。
     *
     * @param category 日程分类，可为空
     * @return 按开始时间升序排列的日历事件列表
     */
    List<CalendarEventResponse> listEvents(EventCategory category);

    /**
     * 根据 ID 查询有效日历事件。
     *
     * @param id 日程 ID
     * @return 日历事件
     */
    CalendarEventResponse getEvent(Long id);

    /**
     * 更新日历事件。
     *
     * @param id 日程 ID
     * @param request 更新请求
     * @return 更新后的日历事件
     */
    CalendarEventResponse updateEvent(Long id, CalendarEventUpdateRequest request);

    /**
     * 删除日历事件。
     *
     * @param id 日程 ID
     */
    void deleteEvent(Long id);
}
