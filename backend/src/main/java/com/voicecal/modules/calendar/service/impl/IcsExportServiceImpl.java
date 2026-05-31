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

    private static final String CRLF = "\r\n";
    private static final int ICS_LINE_LIMIT = 75;
    private static final DateTimeFormatter UTC_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private final CalendarEventRepository calendarEventRepository;
    private final Clock clock;
    private final ZoneId eventZoneId;

    public IcsExportServiceImpl(CalendarEventRepository calendarEventRepository, Clock clock) {
        this.calendarEventRepository = calendarEventRepository;
        this.clock = clock;
        this.eventZoneId = ZoneId.systemDefault();
    }

    /**
     * 导出单个有效日程。
     *
     * @param eventId 日程 ID
     * @return ICS 文本
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
     * 导出指定时间范围内的有效日程。
     *
     * @param startTime 查询开始时间
     * @param endTime 查询结束时间
     * @return ICS 文本
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
        events.forEach(event -> appendEvent(builder, event));
        appendLine(builder, "END:VCALENDAR");
        return builder.toString();
    }

    private void appendEvent(StringBuilder builder, CalendarEvent event) {
        appendLine(builder, "BEGIN:VEVENT");
        appendLine(builder, "UID:event-" + event.getId() + "@voicecal-agent");
        appendLine(builder, "DTSTAMP:" + formatNowAsUtc());
        appendLine(builder, "DTSTART:" + formatAsUtc(event.getStartTime()));
        appendLine(builder, "DTEND:" + formatAsUtc(event.getEndTime()));
        appendLine(builder, "SUMMARY:" + escapeText(event.getTitle()));
        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            appendLine(builder, "DESCRIPTION:" + escapeText(event.getDescription()));
        }
        if (event.getLocation() != null && !event.getLocation().isBlank()) {
            appendLine(builder, "LOCATION:" + escapeText(event.getLocation()));
        }
        appendLine(builder, "END:VEVENT");
    }

    private void appendLine(StringBuilder builder, String line) {
        builder.append(foldLine(line)).append(CRLF);
    }

    private String foldLine(String line) {
        if (line.length() <= ICS_LINE_LIMIT) {
            return line;
        }
        StringBuilder builder = new StringBuilder(line.length() + line.length() / ICS_LINE_LIMIT);
        int index = 0;
        while (index < line.length()) {
            int endIndex = Math.min(index + ICS_LINE_LIMIT, line.length());
            if (index > 0) {
                builder.append(CRLF).append(' ');
            }
            builder.append(line, index, endIndex);
            index = endIndex;
        }
        return builder.toString();
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

    private String formatNowAsUtc() {
        return UTC_FORMATTER.format(clock.instant());
    }

    private String formatAsUtc(LocalDateTime value) {
        return UTC_FORMATTER.format(value.atZone(eventZoneId).withZoneSameInstant(ZoneOffset.UTC));
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw CustomException.create(ResultCodeEnum.MISS_PARAMS, "开始时间和结束时间不能为空");
        }
        if (!endTime.isAfter(startTime)) {
            throw CustomException.create(ResultCodeEnum.PARAMS_ERROR, "结束时间必须晚于开始时间");
        }
    }
}
