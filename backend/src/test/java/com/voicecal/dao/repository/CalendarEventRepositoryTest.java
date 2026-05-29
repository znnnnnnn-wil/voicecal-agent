package com.voicecal.dao.repository;

import com.voicecal.common.enums.dao.EventCategory;
import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.dao.entity.CalendarEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 日程事件 Repository 测试，验证基础数据访问能力。
 */
@ActiveProfiles("h2")
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CalendarEventRepositoryTest {

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Test
    @DisplayName("保存日程成功")
    void shouldSaveCalendarEvent() {
        CalendarEvent event = createEvent("项目评审", "讨论 PR3 领域模型", 9, 10, EventStatus.ACTIVE);

        CalendarEvent savedEvent = calendarEventRepository.saveAndFlush(event);

        assertThat(savedEvent.getId()).isNotNull();
        assertThat(savedEvent.getTimezone()).isEqualTo(CalendarEvent.DEFAULT_TIMEZONE);
        assertThat(savedEvent.getCategory()).isEqualTo(EventCategory.OTHER);
        assertThat(savedEvent.getStatus()).isEqualTo(EventStatus.ACTIVE);
        assertThat(savedEvent.getCreatedAt()).isNotNull();
        assertThat(savedEvent.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("按状态查询 ACTIVE 日程")
    void shouldFindActiveEventsByStatus() {
        calendarEventRepository.save(createEvent("晨会", "同步任务", 9, 10, EventStatus.ACTIVE));
        calendarEventRepository.save(createEvent("已取消会议", "无需参加", 10, 11, EventStatus.CANCELLED));
        calendarEventRepository.save(createEvent("已删除会议", "软删除数据", 11, 12, EventStatus.DELETED));
        calendarEventRepository.flush();

        List<CalendarEvent> events = calendarEventRepository.findByStatusOrderByStartTimeAsc(EventStatus.ACTIVE);

        assertThat(events)
                .extracting(CalendarEvent::getTitle)
                .containsExactly("晨会");
    }

    @Test
    @DisplayName("按时间范围查询有交集的日程")
    void shouldFindEventsOverlappingTimeRange() {
        calendarEventRepository.save(createEvent("上午会议", "有效交集", 9, 10, EventStatus.ACTIVE));
        calendarEventRepository.save(createEvent("下午会议", "不在范围内", 14, 15, EventStatus.ACTIVE));
        calendarEventRepository.save(createEvent("删除会议", "不应返回", 9, 10, EventStatus.DELETED));
        calendarEventRepository.flush();

        List<CalendarEvent> events = calendarEventRepository.findOverlappingEvents(
                LocalDateTime.of(2026, 5, 29, 9, 30),
                LocalDateTime.of(2026, 5, 29, 11, 0),
                EventStatus.ACTIVE
        );

        assertThat(events)
                .extracting(CalendarEvent::getTitle)
                .containsExactly("上午会议");
    }

    @Test
    @DisplayName("按关键词查询标题或描述匹配的日程")
    void shouldSearchEventsByKeyword() {
        calendarEventRepository.save(createEvent("产品面试", "候选人沟通", 9, 10, EventStatus.ACTIVE));
        calendarEventRepository.save(createEvent("技术评审", "讨论面试反馈", 10, 11, EventStatus.ACTIVE));
        calendarEventRepository.save(createEvent("午餐", "团队聚餐", 12, 13, EventStatus.ACTIVE));
        calendarEventRepository.flush();

        List<CalendarEvent> events = calendarEventRepository.searchByKeyword("面试", EventStatus.ACTIVE);

        assertThat(events)
                .extracting(CalendarEvent::getTitle)
                .containsExactly("产品面试", "技术评审");
    }

    @Test
    @DisplayName("查询时不返回 DELETED 状态日程")
    void shouldNotReturnDeletedEventsWhenQueryingActiveEvents() {
        CalendarEvent deletedEvent = calendarEventRepository.saveAndFlush(
                createEvent("已删除日程", "软删除记录", 9, 10, EventStatus.DELETED)
        );

        List<CalendarEvent> activeEvents = calendarEventRepository.findByStatusOrderByStartTimeAsc(EventStatus.ACTIVE);
        List<CalendarEvent> keywordEvents = calendarEventRepository.searchByKeyword("已删除", EventStatus.ACTIVE);

        assertThat(activeEvents).isEmpty();
        assertThat(keywordEvents).isEmpty();
        assertThat(calendarEventRepository.findByIdAndStatus(deletedEvent.getId(), EventStatus.ACTIVE)).isEmpty();
    }

    private CalendarEvent createEvent(
            String title,
            String description,
            int startHour,
            int endHour,
            EventStatus status
    ) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(title);
        event.setDescription(description);
        event.setStartTime(LocalDateTime.of(2026, 5, 29, startHour, 0));
        event.setEndTime(LocalDateTime.of(2026, 5, 29, endHour, 0));
        event.setStatus(status);
        return event;
    }
}
