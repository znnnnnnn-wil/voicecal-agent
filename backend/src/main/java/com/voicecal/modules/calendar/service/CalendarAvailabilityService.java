package com.voicecal.modules.calendar.service;

import com.voicecal.modules.calendar.entity.request.ConflictCheckRequest;
import com.voicecal.modules.calendar.entity.request.FreeTimeQueryRequest;
import com.voicecal.modules.calendar.entity.response.ConflictCheckResponse;
import com.voicecal.modules.calendar.entity.response.FreeTimeSlotResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 日历可用性服务接口，负责冲突检测和空闲时间查询。
 */
public interface CalendarAvailabilityService {

    /**
     * 检测指定时间段是否与已有日程冲突。
     *
     * @param request 冲突检测请求
     * @return 冲突检测结果
     */
    ConflictCheckResponse checkConflicts(ConflictCheckRequest request);

    /**
     * 查询指定时间范围内的空闲时间段。
     *
     * @param request 空闲时间查询请求
     * @return 空闲时间段列表
     */
    List<FreeTimeSlotResponse> findFreeTimeSlots(FreeTimeQueryRequest request);

    /**
     * 查询指定时间范围内的空闲时间段。
     *
     * @param startTime 查询开始时间
     * @param endTime 查询结束时间
     * @param minMinutes 最小空闲分钟数，可为空
     * @return 空闲时间段列表
     */
    List<FreeTimeSlotResponse> findFreeTimeSlots(LocalDateTime startTime, LocalDateTime endTime, Integer minMinutes);
}
