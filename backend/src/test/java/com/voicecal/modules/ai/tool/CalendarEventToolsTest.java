package com.voicecal.modules.ai.tool;

import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import com.voicecal.modules.assistant.pending.PendingActionStore;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 日程工具集成测试。
 */
@ActiveProfiles("h2")
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CalendarEventToolsTest {

    @Autowired
    private CalendarEventTools calendarEventTools;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private PendingActionStore pendingActionStore;

    @BeforeEach
    void setUp() {
        pendingActionStore.clear();
        calendarEventRepository.deleteAll();
        calendarEventRepository.flush();
    }

    @Test
    void listCalendarEvents_shouldReturnExistingEventsOrderedByStartTimeAsc() {
        calendarEventRepository.save(createEvent("下午会议", 15, 16));
        calendarEventRepository.save(createEvent("上午会议", 9, 10));
        calendarEventRepository.flush();

        String result = calendarEventTools.listCalendarEvents();

        assertThat(result).contains("上午会议", "下午会议");
        assertThat(result.indexOf("上午会议")).isLessThan(result.indexOf("下午会议"));
    }

    @Test
    void getCalendarEventById_shouldReturnSpecifiedEvent() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("需求评审", 10, 11));

        String result = calendarEventTools.getCalendarEventById(event.getId());

        assertThat(result)
                .contains("ID: " + event.getId())
                .contains("需求评审")
                .contains("2026-05-29T10:00");
    }

    @Test
    void createCalendarEvent_shouldCreateEvent() {
        String result = calendarEventTools.createCalendarEvent(
                "工具创建会议",
                "通过 AI 工具创建",
                "2026-06-01T10:00:00",
                "2026-06-01T11:00:00",
                "线上"
        );

        assertThat(result).contains("创建日程成功", "工具创建会议", "线上");
        assertThat(calendarEventRepository.findAll()).hasSize(1);
        assertThat(calendarEventRepository.findAll().get(0).getTitle()).isEqualTo("工具创建会议");
    }

    @Test
    void createCalendarEvent_shouldReturnClearError_whenTimeRangeIsInvalid() {
        String result = calendarEventTools.createCalendarEvent(
                "非法时间会议",
                "结束时间不晚于开始时间",
                "2026-06-01T10:00:00",
                "2026-06-01T10:00:00",
                "线上"
        );

        assertThat(result).isEqualTo("创建日程失败：结束时间必须晚于开始时间");
        assertThat(calendarEventRepository.findAll()).isEmpty();
    }

    @Test
    void checkCalendarConflict_shouldReturnConflictSummary() {
        calendarEventRepository.saveAndFlush(createEvent("需求评审", 10, 11));

        String result = calendarEventTools.checkCalendarConflict(
                "2026-05-29T10:30:00",
                "2026-05-29T11:30:00",
                null
        );

        assertThat(result)
                .contains("存在日程冲突")
                .contains("需求评审")
                .contains("2026-05-29T10:00");
    }

    @Test
    void findFreeTime_shouldReturnAvailableSlots() {
        calendarEventRepository.save(createEvent("会议一", 14, 15));
        calendarEventRepository.save(createEvent("会议二", 16, 17));
        calendarEventRepository.flush();

        String result = calendarEventTools.findFreeTime(
                "2026-05-29T13:00:00",
                "2026-05-29T18:00:00",
                30
        );

        assertThat(result)
                .contains("可用空闲时间")
                .contains("开始: 2026-05-29T13:00")
                .contains("结束: 2026-05-29T14:00")
                .contains("开始: 2026-05-29T17:00")
                .contains("结束: 2026-05-29T18:00");
    }

    @Test
    void createPendingDeleteAction_shouldCreateActionWithoutDeletingEvent() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("待删除会议", 10, 11));

        String result = calendarEventTools.createPendingDeleteAction("conversation-a", event.getId());

        assertThat(result)
                .contains("已创建待确认删除操作")
                .contains("操作 ID")
                .contains("待删除会议");
        assertThat(calendarEventRepository.findById(event.getId())).isPresent();
        assertThat(pendingActionStore.findByConversationId("conversation-a")).hasSize(1);
    }

    @Test
    void confirmPendingAction_shouldExecutePendingDeleteAction() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("待删除会议", 10, 11));
        String createResult = calendarEventTools.createPendingDeleteAction("conversation-a", event.getId());
        String actionId = pendingActionStore.findByConversationId("conversation-a").get(0).id();

        String confirmResult = calendarEventTools.confirmPendingAction("conversation-a", actionId);

        assertThat(createResult).contains("已创建待确认删除操作");
        assertThat(confirmResult).isEqualTo("已删除日程");
        assertThat(calendarEventRepository.findById(event.getId())).isEmpty();
    }

    @Test
    void createPendingUpdateAction_shouldCreateActionWithoutUpdatingEvent() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("旧会议", 10, 11));

        String result = calendarEventTools.createPendingUpdateAction(
                "conversation-a",
                event.getId(),
                "新会议",
                "更新描述",
                "2026-05-29T13:00:00",
                "2026-05-29T14:00:00",
                "会议室 A"
        );

        assertThat(result)
                .contains("已创建待确认更新操作")
                .contains("操作 ID")
                .contains("旧会议");
        assertThat(calendarEventRepository.findById(event.getId()).orElseThrow().getTitle()).isEqualTo("旧会议");
        assertThat(pendingActionStore.findByConversationId("conversation-a")).hasSize(1);
    }

    private CalendarEvent createEvent(String title, int startHour, int endHour) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(title);
        event.setDescription(title + "描述");
        event.setStartTime(LocalDateTime.of(2026, 5, 29, startHour, 0));
        event.setEndTime(LocalDateTime.of(2026, 5, 29, endHour, 0));
        event.setLocation("线上");
        event.setStatus(EventStatus.ACTIVE);
        return event;
    }
}
