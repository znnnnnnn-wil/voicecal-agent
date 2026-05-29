package com.voicecal.modules.ai.controller;

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
@ActiveProfiles("h2")
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
        calendarEventRepository.save(createEvent("晨会", date.atTime(9, 0), date.atTime(10, 0)));
        calendarEventRepository.save(createEvent("评审会", date.atTime(11, 0), date.atTime(12, 30)));
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
                .andExpect(jsonPath("$.data.summary").value(
                        "2026-06-01 共有 2 个日程，已占用约 150 分钟。第一个日程从 09:00 开始。"
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
                .andExpect(jsonPath("$.data.summary").value("2026-06-02 没有已安排日程。"))
                .andExpect(jsonPath("$.data.events").isEmpty());
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
