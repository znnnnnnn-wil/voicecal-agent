package com.voicecal.modules.calendar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 日历可用性接口集成测试。
 */
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CalendarAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @BeforeEach
    void setUp() {
        calendarEventRepository.deleteAll();
        calendarEventRepository.flush();
    }

    @Test
    void checkConflicts_shouldReturnNoConflict_whenNoEventsOverlap() throws Exception {
        calendarEventRepository.saveAndFlush(createEvent("已有会议", "2026-05-30T10:00:00", "2026-05-30T11:00:00"));

        mockMvc.perform(post("/api/calendar/events/conflicts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictRequest(
                                "2026-05-30T11:00:00",
                                "2026-05-30T12:00:00",
                                null
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasConflict").value(false))
                .andExpect(jsonPath("$.data.conflicts").isEmpty());
    }

    @Test
    void checkConflicts_shouldReturnConflict_whenEventFullyOverlaps() throws Exception {
        calendarEventRepository.saveAndFlush(createEvent("完整重叠会议", "2026-05-30T10:00:00", "2026-05-30T12:00:00"));

        mockMvc.perform(post("/api/calendar/events/conflicts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictRequest(
                                "2026-05-30T10:00:00",
                                "2026-05-30T12:00:00",
                                null
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasConflict").value(true))
                .andExpect(jsonPath("$.data.conflicts[0].title").value("完整重叠会议"));
    }

    @Test
    void checkConflicts_shouldReturnConflict_whenEventPartiallyOverlapsAtStart() throws Exception {
        calendarEventRepository.saveAndFlush(createEvent("开始重叠会议", "2026-05-30T10:00:00", "2026-05-30T11:00:00"));

        mockMvc.perform(post("/api/calendar/events/conflicts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictRequest(
                                "2026-05-30T09:30:00",
                                "2026-05-30T10:30:00",
                                null
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasConflict").value(true))
                .andExpect(jsonPath("$.data.conflicts[0].title").value("开始重叠会议"));
    }

    @Test
    void checkConflicts_shouldReturnConflict_whenEventPartiallyOverlapsAtEnd() throws Exception {
        calendarEventRepository.saveAndFlush(createEvent("结束重叠会议", "2026-05-30T10:00:00", "2026-05-30T11:00:00"));

        mockMvc.perform(post("/api/calendar/events/conflicts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictRequest(
                                "2026-05-30T10:30:00",
                                "2026-05-30T11:30:00",
                                null
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasConflict").value(true))
                .andExpect(jsonPath("$.data.conflicts[0].title").value("结束重叠会议"));
    }

    @Test
    void checkConflicts_shouldNotTreatTouchingBoundariesAsConflict() throws Exception {
        calendarEventRepository.saveAndFlush(createEvent("边界会议", "2026-05-30T10:00:00", "2026-05-30T11:00:00"));

        mockMvc.perform(post("/api/calendar/events/conflicts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictRequest(
                                "2026-05-30T11:00:00",
                                "2026-05-30T12:00:00",
                                null
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasConflict").value(false));
    }

    @Test
    void checkConflicts_shouldExcludeEvent_whenExcludeEventIdIsProvided() throws Exception {
        CalendarEvent event = calendarEventRepository.saveAndFlush(
                createEvent("排除自身会议", "2026-05-30T10:00:00", "2026-05-30T11:00:00")
        );

        mockMvc.perform(post("/api/calendar/events/conflicts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictRequest(
                                "2026-05-30T10:00:00",
                                "2026-05-30T11:00:00",
                                event.getId()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasConflict").value(false));
    }

    @Test
    void findFreeTime_shouldReturnFullRange_whenNoEventsExist() throws Exception {
        mockMvc.perform(get("/api/calendar/events/free-time")
                        .param("startTime", "2026-05-30T13:00:00")
                        .param("endTime", "2026-05-30T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].startTime").value("2026-05-30T13:00:00"))
                .andExpect(jsonPath("$.data[0].endTime").value("2026-05-30T18:00:00"))
                .andExpect(jsonPath("$.data[0].minutes").value(300));
    }

    @Test
    void findFreeTime_shouldReturnSlotsBetweenEvents() throws Exception {
        calendarEventRepository.save(createEvent("会议一", "2026-05-30T14:00:00", "2026-05-30T15:00:00"));
        calendarEventRepository.save(createEvent("会议二", "2026-05-30T16:00:00", "2026-05-30T17:00:00"));
        calendarEventRepository.flush();

        mockMvc.perform(get("/api/calendar/events/free-time")
                        .param("startTime", "2026-05-30T13:00:00")
                        .param("endTime", "2026-05-30T18:00:00")
                        .param("minMinutes", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].startTime").value("2026-05-30T13:00:00"))
                .andExpect(jsonPath("$.data[0].endTime").value("2026-05-30T14:00:00"))
                .andExpect(jsonPath("$.data[1].startTime").value("2026-05-30T15:00:00"))
                .andExpect(jsonPath("$.data[1].endTime").value("2026-05-30T16:00:00"))
                .andExpect(jsonPath("$.data[2].startTime").value("2026-05-30T17:00:00"))
                .andExpect(jsonPath("$.data[2].endTime").value("2026-05-30T18:00:00"));
    }

    @Test
    void findFreeTime_shouldIgnoreSlotsShorterThanMinMinutes() throws Exception {
        calendarEventRepository.save(createEvent("会议一", "2026-05-30T13:00:00", "2026-05-30T13:45:00"));
        calendarEventRepository.save(createEvent("会议二", "2026-05-30T14:10:00", "2026-05-30T18:00:00"));
        calendarEventRepository.flush();

        mockMvc.perform(get("/api/calendar/events/free-time")
                        .param("startTime", "2026-05-30T13:00:00")
                        .param("endTime", "2026-05-30T18:00:00")
                        .param("minMinutes", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void findFreeTime_shouldHandleOverlappingBusyEvents() throws Exception {
        calendarEventRepository.save(createEvent("会议一", "2026-05-30T14:00:00", "2026-05-30T15:30:00"));
        calendarEventRepository.save(createEvent("会议二", "2026-05-30T15:00:00", "2026-05-30T16:00:00"));
        calendarEventRepository.flush();

        mockMvc.perform(get("/api/calendar/events/free-time")
                        .param("startTime", "2026-05-30T13:00:00")
                        .param("endTime", "2026-05-30T17:00:00")
                        .param("minMinutes", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].startTime").value("2026-05-30T13:00:00"))
                .andExpect(jsonPath("$.data[0].endTime").value("2026-05-30T14:00:00"))
                .andExpect(jsonPath("$.data[1].startTime").value("2026-05-30T16:00:00"))
                .andExpect(jsonPath("$.data[1].endTime").value("2026-05-30T17:00:00"));
    }

    @Test
    void shouldReturnBadRequest_whenEndTimeIsNotAfterStartTime() throws Exception {
        mockMvc.perform(post("/api/calendar/events/conflicts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictRequest(
                                "2026-05-30T10:00:00",
                                "2026-05-30T10:00:00",
                                null
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturnBadRequest_whenMinMinutesIsInvalid() throws Exception {
        mockMvc.perform(get("/api/calendar/events/free-time")
                        .param("startTime", "2026-05-30T13:00:00")
                        .param("endTime", "2026-05-30T18:00:00")
                        .param("minMinutes", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturnBadRequest_whenTimeFormatIsInvalid() throws Exception {
        mockMvc.perform(get("/api/calendar/events/free-time")
                        .param("startTime", "bad-time")
                        .param("endTime", "2026-05-30T18:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private Map<String, Object> conflictRequest(String startTime, String endTime, Long excludeEventId) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("startTime", startTime);
        request.put("endTime", endTime);
        request.put("excludeEventId", excludeEventId);
        return request;
    }

    private CalendarEvent createEvent(String title, String startTime, String endTime) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(title);
        event.setDescription(title + "描述");
        event.setStartTime(LocalDateTime.parse(startTime));
        event.setEndTime(LocalDateTime.parse(endTime));
        event.setLocation("线上");
        event.setStatus(EventStatus.ACTIVE);
        return event;
    }
}
