package com.voicecal.modules.calendar.service;

import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ICS 导出服务集成测试。
 */
@ActiveProfiles("h2")
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class IcsExportServiceTest {

    @Autowired
    private IcsExportService icsExportService;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @BeforeEach
    void setUp() {
        calendarEventRepository.deleteAll();
        calendarEventRepository.flush();
    }

    @Test
    void exportIcs_shouldEscapeSpecialCharacters() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent(
                "Review, sync; planning",
                "Line 1\nLine 2\\path",
                "Zoom, Room; A",
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 11, 0)
        ));

        String ics = icsExportService.exportEvent(event.getId());

        assertThat(ics).contains("SUMMARY:Review\\, sync\\; planning");
        assertThat(ics).contains("DESCRIPTION:Line 1\\nLine 2\\\\path");
        assertThat(ics).contains("LOCATION:Zoom\\, Room\\; A");
    }

    private CalendarEvent createEvent(
            String title,
            String description,
            String location,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(title);
        event.setDescription(description);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setLocation(location);
        event.setStatus(EventStatus.ACTIVE);
        return event;
    }
}
