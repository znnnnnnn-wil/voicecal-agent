package com.voicecal.modules.calendar.controller;

import com.voicecal.common.enums.dao.EventCategory;
import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ICS 导出接口集成测试。
 */
@ActiveProfiles("mysql")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class IcsExportControllerTest {

    private static final MediaType TEXT_CALENDAR = new MediaType("text", "calendar");

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
                "2026-06-01T10:00:00",
                "2026-06-01T11:00:00",
                "Zoom"
        ));

        mockMvc.perform(get("/api/calendar/events/{id}/ics", event.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(TEXT_CALENDAR))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Disposition", containsString("voicecal-event-" + event.getId() + ".ics")))
                .andExpect(content().string(containsString("BEGIN:VCALENDAR")))
                .andExpect(content().string(containsString("VERSION:2.0")))
                .andExpect(content().string(containsString("BEGIN:VEVENT")))
                .andExpect(content().string(containsString("UID:event-" + event.getId() + "@voicecal-agent")))
                .andExpect(content().string(containsString("SUMMARY:Team meeting")))
                .andExpect(content().string(containsString("DESCRIPTION:Weekly sync")))
                .andExpect(content().string(containsString("LOCATION:Zoom")))
                .andExpect(content().string(containsString("DTSTART:")))
                .andExpect(content().string(containsString("DTEND:")))
                .andExpect(content().string(containsString("END:VEVENT")))
                .andExpect(content().string(containsString("END:VCALENDAR")));
    }

    @Test
    void exportSingleEvent_shouldReturnNotFound_whenEventDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/calendar/events/{id}/ics", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void exportRange_shouldReturnMultipleEvents() throws Exception {
        calendarEventRepository.saveAndFlush(createEvent("晨会", null, "2026-06-01T09:00:00", "2026-06-01T09:30:00", null));
        calendarEventRepository.saveAndFlush(createEvent("评审", null, "2026-06-01T14:00:00", "2026-06-01T15:00:00", null));

        String body = mockMvc.perform(get("/api/calendar/events/ics")
                        .param("startTime", "2026-06-01T00:00:00")
                        .param("endTime", "2026-06-02T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(TEXT_CALENDAR))
                .andExpect(header().string("Content-Disposition", containsString("voicecal-events.ics")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("SUMMARY:晨会", "SUMMARY:评审");
        assertThat(body.split("BEGIN:VEVENT", -1)).hasSize(3);
    }

    @Test
    void exportRange_shouldUseOverlapQuery() throws Exception {
        calendarEventRepository.saveAndFlush(createEvent("跨天会议", null, "2026-06-01T23:00:00", "2026-06-02T01:00:00", null));

        mockMvc.perform(get("/api/calendar/events/ics")
                        .param("startTime", "2026-06-02T00:00:00")
                        .param("endTime", "2026-06-02T23:59:00"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SUMMARY:跨天会议")));
    }

    @Test
    void exportRange_shouldReturnBadRequest_whenEndTimeIsNotAfterStartTime() throws Exception {
        mockMvc.perform(get("/api/calendar/events/ics")
                        .param("startTime", "2026-06-02T00:00:00")
                        .param("endTime", "2026-06-02T00:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exportIcs_shouldEscapeSpecialCharacters() throws Exception {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent(
                "Review, sync; planning",
                "Line 1\nLine 2\\done",
                "2026-06-01T10:00:00",
                "2026-06-01T11:00:00",
                "Room, A"
        ));

        mockMvc.perform(get("/api/calendar/events/{id}/ics", event.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SUMMARY:Review\\, sync\\; planning")))
                .andExpect(content().string(containsString("DESCRIPTION:Line 1\\nLine 2\\\\done")))
                .andExpect(content().string(containsString("LOCATION:Room\\, A")));
    }

    @Test
    void exportIcs_shouldUseTextCalendarContentType() throws Exception {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent(
                "类型测试",
                null,
                "2026-06-01T10:00:00",
                "2026-06-01T11:00:00",
                null
        ));

        mockMvc.perform(get("/api/calendar/events/{id}/ics", event.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(TEXT_CALENDAR));
    }

    private CalendarEvent createEvent(
            String title,
            String description,
            String startTime,
            String endTime,
            String location
    ) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(title);
        event.setDescription(description);
        event.setStartTime(LocalDateTime.parse(startTime));
        event.setEndTime(LocalDateTime.parse(endTime));
        event.setLocation(location);
        event.setCategory(EventCategory.MEETING);
        event.setStatus(EventStatus.ACTIVE);
        event.setReminderTriggered(false);
        return event;
    }
}
