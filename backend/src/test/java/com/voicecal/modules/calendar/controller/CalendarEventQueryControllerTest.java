package com.voicecal.modules.calendar.controller;

import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 日历事件日期查询接口集成测试。
 */
@ActiveProfiles("mysql")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CalendarEventQueryControllerTest {

    private static final ZoneId TEST_ZONE_ID = ZoneId.of("UTC");

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
    void todayEvents_shouldReturnEventsForCurrentDate() throws Exception {
        LocalDate today = LocalDate.now(TEST_ZONE_ID);
        calendarEventRepository.saveAndFlush(createEvent(
                "今日日程",
                today.atTime(9, 0),
                today.atTime(10, 0)
        ));

        mockMvc.perform(get("/api/calendar/events/today")
                        .param("timezone", TEST_ZONE_ID.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("今日日程"));
    }

    @Test
    void todayEvents_shouldIncludeOverlappingEvents() throws Exception {
        LocalDate today = LocalDate.now(TEST_ZONE_ID);
        calendarEventRepository.saveAndFlush(createEvent(
                "跨天日程",
                today.minusDays(1).atTime(23, 0),
                today.atTime(1, 0)
        ));

        mockMvc.perform(get("/api/calendar/events/today")
                        .param("timezone", TEST_ZONE_ID.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("跨天日程"));
    }

    @Test
    void todayEvents_shouldExcludeEventsOutsideToday() throws Exception {
        LocalDate today = LocalDate.now(TEST_ZONE_ID);
        calendarEventRepository.save(createEvent(
                "昨日普通日程",
                today.minusDays(1).atTime(9, 0),
                today.minusDays(1).atTime(10, 0)
        ));
        calendarEventRepository.save(createEvent(
                "明日普通日程",
                today.plusDays(1).atTime(9, 0),
                today.plusDays(1).atTime(10, 0)
        ));
        calendarEventRepository.flush();

        mockMvc.perform(get("/api/calendar/events/today")
                        .param("timezone", TEST_ZONE_ID.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void weekEvents_shouldReturnEventsForCurrentWeek() throws Exception {
        LocalDate weekStart = LocalDate.now(TEST_ZONE_ID)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        calendarEventRepository.saveAndFlush(createEvent(
                "本周日程",
                weekStart.plusDays(2).atTime(10, 0),
                weekStart.plusDays(2).atTime(11, 0)
        ));

        mockMvc.perform(get("/api/calendar/events/week")
                        .param("timezone", TEST_ZONE_ID.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("本周日程"));
    }

    @Test
    void weekEvents_shouldUseMondayAsWeekStart() throws Exception {
        LocalDate weekStart = LocalDate.now(TEST_ZONE_ID)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        calendarEventRepository.save(createEvent(
                "周一开始日程",
                weekStart.atTime(0, 0),
                weekStart.atTime(1, 0)
        ));
        calendarEventRepository.save(createEvent(
                "下周一边界日程",
                weekStart.plusWeeks(1).atTime(0, 0),
                weekStart.plusWeeks(1).atTime(1, 0)
        ));
        calendarEventRepository.flush();

        mockMvc.perform(get("/api/calendar/events/week")
                        .param("timezone", TEST_ZONE_ID.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("周一开始日程"));
    }

    @Test
    void events_shouldReturnBadRequest_whenTimezoneIsInvalid() throws Exception {
        mockMvc.perform(get("/api/calendar/events/today")
                        .param("timezone", "Invalid/Zone"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void byDateEvents_shouldReturnEventsForGivenDate() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 1);
        calendarEventRepository.saveAndFlush(createEvent(
                "指定日期日程",
                date.atTime(9, 0),
                date.atTime(10, 0)
        ));

        mockMvc.perform(get("/api/calendar/events/by-date")
                        .param("date", date.toString())
                        .param("timezone", TEST_ZONE_ID.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("指定日期日程"));
    }

    @Test
    void byDateEvents_shouldReturnBadRequest_whenDateIsInvalid() throws Exception {
        mockMvc.perform(get("/api/calendar/events/by-date")
                        .param("date", "bad-date")
                        .param("timezone", TEST_ZONE_ID.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private CalendarEvent createEvent(String title, LocalDateTime startTime, LocalDateTime endTime) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(title);
        event.setDescription(title + "描述");
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setLocation("线上");
        event.setStatus(EventStatus.ACTIVE);
        return event;
    }
}
