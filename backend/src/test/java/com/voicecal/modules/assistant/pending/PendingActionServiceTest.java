package com.voicecal.modules.assistant.pending;

import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.common.exception.CustomException;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import com.voicecal.modules.calendar.entity.request.CalendarEventUpdateRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 待确认操作服务集成测试。
 */
@ActiveProfiles("h2")
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PendingActionServiceTest {

    @Autowired
    private PendingActionService pendingActionService;

    @Autowired
    private PendingActionStore pendingActionStore;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @BeforeEach
    void setUp() {
        pendingActionStore.clear();
        calendarEventRepository.deleteAll();
        calendarEventRepository.flush();
    }

    @Test
    void createPendingDeleteAction_shouldCreatePendingActionWithoutDeletingEvent() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("产品评审", 14, 15));

        PendingActionResponse response = pendingActionService.createPendingDeleteAction("conversation-a", event.getId());

        assertThat(response.id()).isNotBlank();
        assertThat(response.actionType()).isEqualTo(PendingActionType.DELETE_EVENT);
        assertThat(response.targetSummary()).contains("Delete event", "产品评审");
        assertThat(calendarEventRepository.findById(event.getId())).isPresent();
    }

    @Test
    void confirmPendingDeleteAction_shouldDeleteEvent() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("产品评审", 14, 15));
        PendingActionResponse response = pendingActionService.createPendingDeleteAction("conversation-a", event.getId());

        String result = pendingActionService.confirmPendingAction("conversation-a", response.id());

        assertThat(result).isEqualTo("已删除日程");
        assertThat(calendarEventRepository.findById(event.getId())).isEmpty();
        assertThat(pendingActionStore.findById(response.id())).isEmpty();
    }

    @Test
    void cancelPendingDeleteAction_shouldNotDeleteEvent() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("产品评审", 14, 15));
        PendingActionResponse response = pendingActionService.createPendingDeleteAction("conversation-a", event.getId());

        String result = pendingActionService.cancelPendingAction("conversation-a", response.id());

        assertThat(result).contains("已取消待确认操作");
        assertThat(calendarEventRepository.findById(event.getId())).isPresent();
        assertThat(pendingActionStore.findById(response.id())).isEmpty();
    }

    @Test
    void confirmPendingAction_shouldFail_whenActionExpired() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("产品评审", 14, 15));
        PendingAction expiredAction = new PendingAction(
                "expired-action",
                "conversation-a",
                PendingActionType.DELETE_EVENT,
                "{\"eventId\":" + event.getId() + "}",
                "Delete event \"产品评审\"",
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().minusMinutes(11)
        );
        pendingActionStore.save(expiredAction);

        assertThatThrownBy(() -> pendingActionService.confirmPendingAction("conversation-a", expiredAction.id()))
                .isInstanceOf(CustomException.class)
                .hasMessage("待确认操作已过期");
        assertThat(calendarEventRepository.findById(event.getId())).isPresent();
        assertThat(pendingActionStore.findById(expiredAction.id())).isEmpty();
    }

    @Test
    void confirmPendingAction_shouldFail_whenConversationIdDoesNotMatch() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("产品评审", 14, 15));
        PendingActionResponse response = pendingActionService.createPendingDeleteAction("conversation-a", event.getId());

        assertThatThrownBy(() -> pendingActionService.confirmPendingAction("conversation-b", response.id()))
                .isInstanceOf(CustomException.class)
                .hasMessage("待确认操作不存在");
        assertThat(calendarEventRepository.findById(event.getId())).isPresent();
        assertThat(pendingActionStore.findById(response.id())).isPresent();
    }

    @Test
    void createPendingUpdateAction_shouldCreatePendingActionWithoutUpdatingEvent() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("旧会议", 9, 10));

        PendingActionResponse response = pendingActionService.createPendingUpdateAction(
                "conversation-a",
                event.getId(),
                updateRequest("新会议", 11, 12)
        );

        assertThat(response.id()).isNotBlank();
        assertThat(response.actionType()).isEqualTo(PendingActionType.UPDATE_EVENT);
        CalendarEvent unchangedEvent = calendarEventRepository.findById(event.getId()).orElseThrow();
        assertThat(unchangedEvent.getTitle()).isEqualTo("旧会议");
        assertThat(unchangedEvent.getStartTime()).isEqualTo(LocalDateTime.of(2026, 6, 1, 9, 0));
    }

    @Test
    void confirmPendingUpdateAction_shouldUpdateEvent() {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("旧会议", 9, 10));
        PendingActionResponse response = pendingActionService.createPendingUpdateAction(
                "conversation-a",
                event.getId(),
                updateRequest("新会议", 11, 12)
        );

        String result = pendingActionService.confirmPendingAction("conversation-a", response.id());

        assertThat(result).isEqualTo("已更新日程：新会议");
        CalendarEvent updatedEvent = calendarEventRepository.findById(event.getId()).orElseThrow();
        assertThat(updatedEvent.getTitle()).isEqualTo("新会议");
        assertThat(updatedEvent.getStartTime()).isEqualTo(LocalDateTime.of(2026, 6, 1, 11, 0));
        assertThat(updatedEvent.getEndTime()).isEqualTo(LocalDateTime.of(2026, 6, 1, 12, 0));
        assertThat(pendingActionStore.findById(response.id())).isEmpty();
    }

    private CalendarEvent createEvent(String title, int startHour, int endHour) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(title);
        event.setDescription(title + "描述");
        event.setStartTime(LocalDateTime.of(2026, 6, 1, startHour, 0));
        event.setEndTime(LocalDateTime.of(2026, 6, 1, endHour, 0));
        event.setLocation("线上");
        event.setStatus(EventStatus.ACTIVE);
        return event;
    }

    private CalendarEventUpdateRequest updateRequest(String title, int startHour, int endHour) {
        return new CalendarEventUpdateRequest(
                title,
                title + "描述",
                LocalDateTime.of(2026, 6, 1, startHour, 0),
                LocalDateTime.of(2026, 6, 1, endHour, 0),
                "会议室 A",
                null
        );
    }
}
