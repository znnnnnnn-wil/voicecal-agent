package com.voicecal.modules.calendar.service;

import java.time.LocalDateTime;

/**
 * ICS 日历文件导出服务。
 */
public interface IcsExportService {

    /**
     * 导出单个日程的 ICS 内容。
     *
     * @param eventId 日程 ID
     * @return ICS 文本内容
     */
    String exportEvent(Long eventId);

    /**
     * 导出指定时间范围内的日程 ICS 内容。
     *
     * @param startTime 查询开始时间
     * @param endTime 查询结束时间
     * @return ICS 文本内容
     */
    String exportEvents(LocalDateTime startTime, LocalDateTime endTime);
}
