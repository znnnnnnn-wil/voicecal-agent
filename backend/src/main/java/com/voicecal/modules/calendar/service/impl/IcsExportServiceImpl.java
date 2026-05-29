package com.voicecal.modules.calendar.service.impl;

import com.voicecal.common.enums.ResultCodeEnum;
import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.common.exception.CustomException;
import com.voicecal.common.exception.ResourceNotFoundException;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import com.voicecal.modules.calendar.service.IcsExportService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ICS 日历文件导出服务实现。
 */
@Service
public class IcsExportServiceImpl implements IcsExportService {

    private static final DateTimeFormatter ICS_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
    private static final int FOLD_LIMIT = 75;

    private final CalendarEventRepository calendarEventRepository;
    private final Clock clock;
    private final ZoneId eventZoneId;

    public IcsExportServiceImpl(CalendarEventRepository calendarEventRepository, Clock clock) {
        this.calendarEventRepository = calendarEventRepository;
        this.clock = clock;
        this.eventZoneId = ZoneId.systemDefault();
    }

    /**
     * 导出单个日程的 ICS 内容。
     *
     * @param eventId 日程 ID
     * @return ICS 文本内容
     */
    @Override
    @Transactional(readOnly = true)
    public String exportEvent(Long eventId) {
        CalendarEvent event = calendarEventRepository.findByIdAndStatus(eventId, EventStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResultCodeEnum.CALENDAR_EVENT_NOT_FOUND,
                        "日历事件不存在"
                ));
        return generateCalendar(List.of(event));
    }

    /**
     * 导出指定时间范围内的日程 ICS 内容。
     *
     * @param startTime 查询开始时间
     * @param endTime 查询结束时间
     * @return ICS 文本内容
     */
    @Override
    @Transactional(readOnly = true)
    public String exportEvents(LocalDateTime startTime, LocalDateTime endTime) {
        validateTimeRange(startTime, endTime);
        List<CalendarEvent> events = calendarEventRepository.findOverlappingEvents(
                startTime,
                endTime,
                EventStatus.ACTIVE
        );
        return generateCalendar(events);
    }

    private String generateCalendar(List<CalendarEvent> events) {
        StringBuilder builder = new StringBuilder();
        appendLine(builder, "BEGIN:VCALENDAR");
        appendLine(builder, "VERSION:2.0");
        appendLine(builder, "PRODID:-//VoiceCal Agent//CN");
        appendLine(builder, "CALSCALE:GREGORIAN");
        appendLine(builder, "METHOD:PUBLISH");
        for (CalendarEvent event : events) {
            appendEvent(builder, event);
        }
        appendLine(builder, "END:VCALENDAR");
        return builder.toString();
    }

    private void appendEvent(StringBuilder builder, CalendarEvent event) {
        appendLine(builder, "BEGIN:VEVENT");
        appendLine(builder, "UID:event-" + event.getId() + "@voicecal-agent");
        appendLine(builder, "DTSTAMP:" + formatInstantNow());
        appendLine(builder, "DTSTART:" + formatLocalDateTime(event.getStartTime()));
        appendLine(builder, "DTEND:" + formatLocalDateTime(event.getEndTime()));
        appendLine(builder, "SUMMARY:" + escapeText(event.getTitle()));
        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            appendLine(builder, "DESCRIPTION:" + escapeText(event.getDescription()));
        }
        if (event.getLocation() != null && !event.getLocation().isBlank()) {
            appendLine(builder, "LOCATION:" + escapeText(event.getLocation()));
        }
        appendLine(builder, "END:VEVENT");
    }

    private String formatInstantNow() {
        return ICS_DATE_TIME_FORMATTER.format(clock.instant());
    }

    private String formatLocalDateTime(LocalDateTime value) {
        return ICS_DATE_TIME_FORMATTER.format(value.atZone(eventZoneId).withZoneSameInstant(ZoneOffset.UTC));
    }

    private String escapeText(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n")
                .replace("\r", "\\n")
                .replace(";", "\\;")
                .replace(",", "\\,");
    }

    private void appendLine(StringBuilder builder, String line) {
        builder.append(foldLine(line)).append("\r\n");
    }

    private String foldLine(String line) {
        if (line.length() <= FOLD_LIMIT) {
            return line;
        }

        StringBuilder builder = new StringBuilder();
        int start = 0;
        int limit = FOLD_LIMIT;
        while (line.length() - start > limit) {
            if (start > 0) {
                builder.append(' ');
            }
            builder.append(line, start, start + limit).append("\r\n");
            start += limit;
            limit = FOLD_LIMIT - 1;
        }
        if (start > 0) {
            builder.append(' ');
        }
        builder.append(line.substring(start));
        return builder.toString();
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw CustomException.create(ResultCodeEnum.MISS_PARAMS, "startTime 和 endTime 参数不能为空");
        }
        if (!endTime.isAfter(startTime)) {
            throw CustomException.create(ResultCodeEnum.PARAMS_ERROR, "结束时间必须晚于开始时间");
        }
    }
}
