package com.voicecal.modules.calendar.controller;

import com.voicecal.modules.calendar.service.IcsExportService;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ICS 日历文件导出接口。
 */
@RestController
@RequestMapping("/api/calendar/events")
public class IcsExportController {

    private static final MediaType TEXT_CALENDAR = MediaType.parseMediaType("text/calendar; charset=UTF-8");

    private final IcsExportService icsExportService;

    public IcsExportController(IcsExportService icsExportService) {
        this.icsExportService = icsExportService;
    }

    /**
     * 导出单个日程 ICS 文件。
     *
     * @param id 日程 ID
     * @return ICS 文件内容
     */
    @GetMapping("/{id}/ics")
    public ResponseEntity<String> exportEvent(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(TEXT_CALENDAR)
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment("voicecal-event-" + id + ".ics"))
                .body(icsExportService.exportEvent(id));
    }

    /**
     * 导出指定时间范围内的日程 ICS 文件。
     *
     * @param startTime 查询开始时间
     * @param endTime 查询结束时间
     * @return ICS 文件内容
     */
    @GetMapping("/ics")
    public ResponseEntity<String> exportEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        return ResponseEntity.ok()
                .contentType(TEXT_CALENDAR)
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment("voicecal-events.ics"))
                .body(icsExportService.exportEvents(startTime, endTime));
    }

    private String attachment(String filename) {
        return ContentDisposition.attachment()
                .filename(filename)
                .build()
                .toString();
    }
}
