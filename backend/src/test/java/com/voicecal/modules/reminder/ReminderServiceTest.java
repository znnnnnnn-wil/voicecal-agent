package com.voicecal.modules.reminder;

import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import com.voicecal.modules.reminder.service.ReminderService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 日程提醒服务集成测试。
 */
@ActiveProfiles("test")
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ReminderServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 10, 0);

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @BeforeEach
    void setUp() {
        calendarEventRepository.deleteAll();
        calendarEventRepository.flush();
    }

    @Test
    void triggerDueReminders_shouldNotTrigger_whenReminderTimeNotReached() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("未到提醒时间", NOW.plusMinutes(30), 15));

        int count = reminderService.triggerDueReminders();

        CalendarEvent savedEvent = calendarEventRepository.findById(event.getId()).orElseThrow();
        assertThat(count).isZero();
        assertThat(savedEvent.getReminderTriggered()).isFalse();
        assertThat(savedEvent.getRemindedAt()).isNull();
    }

    @Test
    void triggerDueReminders_shouldTrigger_whenReminderTimeReached() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("到达提醒时间", NOW.plusMinutes(10), 15));

        int count = reminderService.triggerDueReminders();

        CalendarEvent savedEvent = calendarEventRepository.findById(event.getId()).orElseThrow();
        assertThat(count).isEqualTo(1);
        assertThat(savedEvent.getReminderTriggered()).isTrue();
        assertThat(savedEvent.getRemindedAt()).isEqualTo(NOW);
    }

    @Test
    void triggerDueReminders_shouldNotTriggerAgain_whenAlreadyTriggered() {
        CalendarEvent event = createEvent("已提醒日程", NOW.plusMinutes(10), 15);
        event.setReminderTriggered(true);
        event.setRemindedAt(NOW.minusMinutes(1));
        CalendarEvent saved = calendarEventRepository.saveAndFlush(event);

        int count = reminderService.triggerDueReminders();

        CalendarEvent savedEvent = calendarEventRepository.findById(saved.getId()).orElseThrow();
        assertThat(count).isZero();
        assertThat(savedEvent.getReminderTriggered()).isTrue();
        assertThat(savedEvent.getRemindedAt()).isEqualTo(NOW.minusMinutes(1));
    }

    @Test
    void triggerDueReminders_shouldIgnoreEventsWithoutReminderMinutes() {
        CalendarEvent event = createEvent("无提醒日程", NOW.plusMinutes(10), null);
        CalendarEvent saved = calendarEventRepository.saveAndFlush(event);

        int count = reminderService.triggerDueReminders();

        CalendarEvent savedEvent = calendarEventRepository.findById(saved.getId()).orElseThrow();
        assertThat(count).isZero();
        assertThat(savedEvent.getReminderTriggered()).isFalse();
        assertThat(savedEvent.getRemindedAt()).isNull();
    }

    @Test
    void triggerDueReminders_shouldNotTrigger_whenEventAlreadyStarted() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("已开始日程", NOW.minusMinutes(1), 15));

        int count = reminderService.triggerDueReminders();

        CalendarEvent savedEvent = calendarEventRepository.findById(event.getId()).orElseThrow();
        assertThat(count).isZero();
        assertThat(savedEvent.getReminderTriggered()).isFalse();
        assertThat(savedEvent.getRemindedAt()).isNull();
    }

    @Test
    void triggerDueReminders_shouldTriggerZeroMinuteReminder_whenEventJustStarted() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("到点提醒", NOW.minusMinutes(1), 0));

        int count = reminderService.triggerDueReminders();

        CalendarEvent savedEvent = calendarEventRepository.findById(event.getId()).orElseThrow();
        assertThat(count).isEqualTo(1);
        assertThat(savedEvent.getReminderTriggered()).isTrue();
        assertThat(savedEvent.getRemindedAt()).isEqualTo(NOW);
    }

    @Test
    void triggerDueReminders_shouldNotTriggerZeroMinuteReminder_whenGraceWindowExpired() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("过期到点提醒", NOW.minusMinutes(6), 0));

        int count = reminderService.triggerDueReminders();

        CalendarEvent savedEvent = calendarEventRepository.findById(event.getId()).orElseThrow();
        assertThat(count).isZero();
        assertThat(savedEvent.getReminderTriggered()).isFalse();
        assertThat(savedEvent.getRemindedAt()).isNull();
    }

    private CalendarEvent createEvent(String title, LocalDateTime startTime, Integer reminderMinutes) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(title);
        event.setDescription(title + "描述");
        event.setStartTime(startTime);
        event.setEndTime(startTime.plusHours(1));
        event.setLocation("线上");
        event.setReminderMinutes(reminderMinutes);
        event.setReminderTriggered(false);
        event.setStatus(EventStatus.ACTIVE);
        return event;
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-06-01T10:00:00Z"), ZoneId.of("UTC"));
        }
    }
}
