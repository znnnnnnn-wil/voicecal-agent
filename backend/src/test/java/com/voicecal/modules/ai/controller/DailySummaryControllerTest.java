package com.voicecal.modules.ai.controller;

import com.voicecal.common.enums.dao.EventCategory;
import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
 * 每日摘要接口集成测试。
 */
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DailySummaryControllerTest {

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
    void dailySummary_shouldReturnSummaryForDate() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 1);
        calendarEventRepository.save(createEvent("晨会", date.atTime(9, 0), date.atTime(10, 0), EventCategory.MEETING));
        calendarEventRepository.save(createEvent("评审会", date.atTime(11, 0), date.atTime(12, 30), EventCategory.MEETING));
        calendarEventRepository.flush();

        mockMvc.perform(get("/api/ai/daily-summary")
                        .param("date", date.toString())
                        .param("timezone", "UTC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.date").value(date.toString()))
                .andExpect(jsonPath("$.data.timezone").value("UTC"))
                .andExpect(jsonPath("$.data.eventCount").value(2))
                .andExpect(jsonPath("$.data.busyMinutes").value(150))
                .andExpect(jsonPath("$.data.categoryStats.MEETING").value(2))
                .andExpect(jsonPath("$.data.earliestEvent.title").value("晨会"))
                .andExpect(jsonPath("$.data.latestEvent.title").value("评审会"))
                .andExpect(jsonPath("$.data.summary").value(
                        "2026-06-01 共有 2 个日程，已占用约 150 分钟。主要安排集中在 MEETING 分类，第一个日程是「晨会」，从 09:00 开始。"
                ))
                .andExpect(jsonPath("$.data.events.length()").value(2));
    }

    @Test
    void dailySummary_shouldReturnEmptySummary_whenNoEvents() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 2);

        mockMvc.perform(get("/api/ai/daily-summary")
                        .param("date", date.toString())
                        .param("timezone", "UTC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.date").value(date.toString()))
                .andExpect(jsonPath("$.data.eventCount").value(0))
                .andExpect(jsonPath("$.data.busyMinutes").value(0))
                .andExpect(jsonPath("$.data.categoryStats").isEmpty())
                .andExpect(jsonPath("$.data.earliestEvent").doesNotExist())
                .andExpect(jsonPath("$.data.latestEvent").doesNotExist())
                .andExpect(jsonPath("$.data.summary").value("2026-06-02 没有已安排日程，你今天的时间比较开放。"))
                .andExpect(jsonPath("$.data.events").isEmpty());
    }

    @Test
    void dailySummary_shouldReturnCategoryStats() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 3);
        calendarEventRepository.save(createEvent("晨会", date.atTime(9, 0), date.atTime(10, 0), EventCategory.MEETING));
        calendarEventRepository.save(createEvent("评审", date.atTime(11, 0), date.atTime(12, 0), EventCategory.MEETING));
        calendarEventRepository.save(createEvent("项目开发", date.atTime(14, 0), date.atTime(15, 0), EventCategory.WORK));
        calendarEventRepository.flush();

        mockMvc.perform(get("/api/ai/daily-summary")
                        .param("date", date.toString())
                        .param("timezone", "UTC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryStats.MEETING").value(2))
                .andExpect(jsonPath("$.data.categoryStats.WORK").value(1));
    }

    @Test
    void dailySummary_shouldReturnEventsOrderedByStartTime() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 4);
        calendarEventRepository.save(createEvent("下午", date.atTime(14, 0), date.atTime(15, 0), EventCategory.WORK));
        calendarEventRepository.save(createEvent("上午", date.atTime(9, 0), date.atTime(10, 0), EventCategory.MEETING));
        calendarEventRepository.flush();

        mockMvc.perform(get("/api/ai/daily-summary")
                        .param("date", date.toString())
                        .param("timezone", "UTC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.events[0].title").value("上午"))
                .andExpect(jsonPath("$.data.events[1].title").value("下午"));
    }

    @Test
    void dailySummary_shouldReturnEarliestAndLatestEvent() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 5);
        calendarEventRepository.save(createEvent("中午", date.atTime(12, 0), date.atTime(13, 0), EventCategory.LIFE));
        calendarEventRepository.save(createEvent("最早", date.atTime(8, 0), date.atTime(9, 0), EventCategory.WORK));
        calendarEventRepository.save(createEvent("最晚结束", date.atTime(17, 0), date.atTime(19, 0), EventCategory.MEETING));
        calendarEventRepository.flush();

        mockMvc.perform(get("/api/ai/daily-summary")
                        .param("date", date.toString())
                        .param("timezone", "UTC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.earliestEvent.title").value("最早"))
                .andExpect(jsonPath("$.data.latestEvent.title").value("最晚结束"));
    }

    private CalendarEvent createEvent(String title, LocalDateTime startTime, LocalDateTime endTime) {
        return createEvent(title, startTime, endTime, EventCategory.OTHER);
    }

    private CalendarEvent createEvent(
            String title,
            LocalDateTime startTime,
            LocalDateTime endTime,
            EventCategory category
    ) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(title);
        event.setDescription(title + "描述");
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setLocation("线上");
        event.setCategory(category);
        event.setStatus(EventStatus.ACTIVE);
        return event;
    }
}
