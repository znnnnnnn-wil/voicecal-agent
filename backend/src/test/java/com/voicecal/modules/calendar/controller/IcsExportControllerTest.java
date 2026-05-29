package com.voicecal.modules.calendar.controller;

import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ICS 导出接口集成测试。
 */
@ActiveProfiles("h2")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class IcsExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @BeforeEach
    void setUp() {
        calendarEventRepository.deleteAll();
        calendarEventRepository.flush();
    }

    @Test
    void exportSingleEvent_shouldReturnIcsContent() throws Exception {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent(
                "Team meeting",
                "Weekly sync",
                "Zoom",
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 11, 0)
        ));

        MvcResult result = mockMvc.perform(get("/api/calendar/events/{id}/ics", event.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/calendar")))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Disposition", containsString("voicecal-event-" + event.getId() + ".ics")))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains(
                "BEGIN:VCALENDAR",
                "VERSION:2.0",
                "BEGIN:VEVENT",
                "UID:event-" + event.getId() + "@voicecal-agent",
                "DTSTART:",
                "DTEND:",
                "SUMMARY:Team meeting",
                "END:VEVENT",
                "END:VCALENDAR"
        );
    }

    @Test
    void exportSingleEvent_shouldReturnNotFound_whenEventDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/calendar/events/{id}/ics", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void exportRange_shouldReturnMultipleEvents() throws Exception {
        calendarEventRepository.save(createEvent(
                "Morning meeting",
                "One",
                "Zoom",
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 1, 10, 0)
        ));
        calendarEventRepository.save(createEvent(
                "Afternoon meeting",
                "Two",
                "Room A",
                LocalDateTime.of(2026, 6, 1, 15, 0),
                LocalDateTime.of(2026, 6, 1, 16, 0)
        ));
        calendarEventRepository.flush();

        MvcResult result = mockMvc.perform(get("/api/calendar/events/ics")
                        .param("startTime", "2026-06-01T00:00:00")
                        .param("endTime", "2026-06-02T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/calendar")))
                .andExpect(header().string("Content-Disposition", containsString("voicecal-events.ics")))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(countOccurrences(body, "BEGIN:VEVENT")).isEqualTo(2);
        assertThat(body).contains("SUMMARY:Morning meeting", "SUMMARY:Afternoon meeting");
    }

    @Test
    void exportRange_shouldUseOverlapQuery() throws Exception {
        calendarEventRepository.saveAndFlush(createEvent(
                "Cross day event",
                "Overlap",
                "Online",
                LocalDateTime.of(2026, 6, 1, 23, 0),
                LocalDateTime.of(2026, 6, 2, 1, 0)
        ));

        MvcResult result = mockMvc.perform(get("/api/calendar/events/ics")
                        .param("startTime", "2026-06-02T00:00:00")
                        .param("endTime", "2026-06-02T23:59:00"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("SUMMARY:Cross day event");
    }

    @Test
    void exportRange_shouldReturnBadRequest_whenEndTimeIsNotAfterStartTime() throws Exception {
        mockMvc.perform(get("/api/calendar/events/ics")
                        .param("startTime", "2026-06-02T00:00:00")
                        .param("endTime", "2026-06-02T00:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exportIcs_shouldUseTextCalendarContentType() throws Exception {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent(
                "Content type test",
                "Description",
                "Online",
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 11, 0)
        ));

        mockMvc.perform(get("/api/calendar/events/{id}/ics", event.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/calendar")));
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

    private int countOccurrences(String text, String target) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(target, index)) >= 0) {
            count++;
            index += target.length();
        }
        return count;
    }
}
