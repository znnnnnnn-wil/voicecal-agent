package com.voicecal.modules.reminder;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 日程提醒接口集成测试。
 */
@ActiveProfiles("mysql")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ReminderControllerTest {

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
    void recentReminders_shouldReturnTriggeredRemindersOrderedByRemindedAtDesc() throws Exception {
        calendarEventRepository.save(createTriggeredEvent(
                "较早提醒",
                LocalDateTime.of(2026, 6, 1, 9, 45)
        ));
        calendarEventRepository.save(createTriggeredEvent(
                "最新提醒",
                LocalDateTime.of(2026, 6, 1, 9, 55)
        ));
        calendarEventRepository.save(createTriggeredEvent(
                "中间提醒",
                LocalDateTime.of(2026, 6, 1, 9, 50)
        ));
        calendarEventRepository.flush();

        mockMvc.perform(get("/api/reminders/recent").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("查询最近提醒成功"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("最新提醒"))
                .andExpect(jsonPath("$.data[1].title").value("中间提醒"));
    }

    private CalendarEvent createTriggeredEvent(String title, LocalDateTime remindedAt) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(title);
        event.setDescription(title + "描述");
        event.setStartTime(LocalDateTime.of(2026, 6, 1, 10, 0));
        event.setEndTime(LocalDateTime.of(2026, 6, 1, 11, 0));
        event.setLocation("线上");
        event.setReminderMinutes(15);
        event.setReminderTriggered(true);
        event.setRemindedAt(remindedAt);
        event.setStatus(EventStatus.ACTIVE);
        return event;
    }
}
