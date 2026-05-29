package com.voicecal.modules.calendar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 日历事件 CRUD API 集成测试。
 */
@ActiveProfiles("h2")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CalendarEventControllerTest {

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
    void createEvent_shouldReturnCreatedEvent_whenRequestIsValid() throws Exception {
        Map<String, Object> request = validRequest(
                "测试会议",
                "PR4 测试",
                "2026-05-29T10:00:00",
                "2026-05-29T11:00:00",
                "线上"
        );

        mockMvc.perform(post("/api/calendar/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("创建日历事件成功"))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.title").value("测试会议"))
                .andExpect(jsonPath("$.data.description").value("PR4 测试"))
                .andExpect(jsonPath("$.data.location").value("线上"));

        assertThat(calendarEventRepository.findAll()).hasSize(1);
        CalendarEvent savedEvent = calendarEventRepository.findAll().get(0);
        assertThat(savedEvent.getTitle()).isEqualTo("测试会议");
        assertThat(savedEvent.getDescription()).isEqualTo("PR4 测试");
        assertThat(savedEvent.getLocation()).isEqualTo("线上");
    }

    @Test
    void createEvent_shouldAcceptReminderMinutes() throws Exception {
        Map<String, Object> request = new HashMap<>(validRequest(
                "提醒会议",
                "包含提醒",
                "2026-05-29T10:00:00",
                "2026-05-29T11:00:00",
                "线上"
        ));
        request.put("reminderMinutes", 15);

        mockMvc.perform(post("/api/calendar/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reminderMinutes").value(15))
                .andExpect(jsonPath("$.data.reminderTriggered").value(false))
                .andExpect(jsonPath("$.data.remindedAt").value(nullValue()));

        CalendarEvent savedEvent = calendarEventRepository.findAll().get(0);
        assertThat(savedEvent.getReminderMinutes()).isEqualTo(15);
        assertThat(savedEvent.getReminderTriggered()).isFalse();
    }

    @Test
    void createEvent_shouldRejectNegativeReminderMinutes() throws Exception {
        Map<String, Object> request = new HashMap<>(validRequest(
                "无效提醒会议",
                "提醒时间为负数",
                "2026-05-29T10:00:00",
                "2026-05-29T11:00:00",
                "线上"
        ));
        request.put("reminderMinutes", -1);

        mockMvc.perform(post("/api/calendar/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_shouldReturnBadRequest_whenTitleIsBlank() throws Exception {
        Map<String, Object> request = validRequest(
                "   ",
                "标题为空",
                "2026-05-29T10:00:00",
                "2026-05-29T11:00:00",
                "线上"
        );

        mockMvc.perform(post("/api/calendar/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_shouldReturnBadRequest_whenEndTimeIsNotAfterStartTime() throws Exception {
        Map<String, Object> request = validRequest(
                "无效时间会议",
                "结束时间不晚于开始时间",
                "2026-05-29T10:00:00",
                "2026-05-29T10:00:00",
                "线上"
        );

        mockMvc.perform(post("/api/calendar/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_shouldReturnBadRequest_whenTimeConflictsWithExistingEvent() throws Exception {
        calendarEventRepository.saveAndFlush(createEvent("已有会议", 10, 11));
        Map<String, Object> request = validRequest(
                "冲突会议",
                "时间与已有会议重叠",
                "2026-05-29T10:30:00",
                "2026-05-29T11:30:00",
                "线上"
        );

        mockMvc.perform(post("/api/calendar/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("日程时间与已有日程冲突"));
    }

    @Test
    void listEvents_shouldReturnEventsOrderedByStartTimeAsc() throws Exception {
        calendarEventRepository.save(createEvent("下午会议", 15, 16));
        calendarEventRepository.save(createEvent("上午会议", 9, 10));
        calendarEventRepository.save(createEvent("午间会议", 12, 13));
        calendarEventRepository.flush();

        mockMvc.perform(get("/api/calendar/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("查询日历事件列表成功"))
                .andExpect(jsonPath("$.data[0].title").value("上午会议"))
                .andExpect(jsonPath("$.data[1].title").value("午间会议"))
                .andExpect(jsonPath("$.data[2].title").value("下午会议"));
    }

    @Test
    void getEvent_shouldReturnEvent_whenEventExists() throws Exception {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("需求评审", 10, 11));

        mockMvc.perform(get("/api/calendar/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("查询日历事件成功"))
                .andExpect(jsonPath("$.data.id").value(event.getId()))
                .andExpect(jsonPath("$.data.title").value("需求评审"));
    }

    @Test
    void getEvent_shouldReturnNotFound_whenEventDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/calendar/events/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEvent_shouldReturnUpdatedEvent_whenRequestIsValid() throws Exception {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("旧会议", 9, 10));
        Map<String, Object> request = validRequest(
                "新会议",
                "更新后的描述",
                "2026-05-29T14:00:00",
                "2026-05-29T15:00:00",
                "会议室 A"
        );

        mockMvc.perform(put("/api/calendar/events/{id}", event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("更新日历事件成功"))
                .andExpect(jsonPath("$.data.id").value(event.getId()))
                .andExpect(jsonPath("$.data.title").value("新会议"))
                .andExpect(jsonPath("$.data.description").value("更新后的描述"))
                .andExpect(jsonPath("$.data.location").value("会议室 A"));

        CalendarEvent updatedEvent = calendarEventRepository.findById(event.getId()).orElseThrow();
        assertThat(updatedEvent.getTitle()).isEqualTo("新会议");
        assertThat(updatedEvent.getDescription()).isEqualTo("更新后的描述");
        assertThat(updatedEvent.getStartTime()).isEqualTo(LocalDateTime.of(2026, 5, 29, 14, 0));
        assertThat(updatedEvent.getEndTime()).isEqualTo(LocalDateTime.of(2026, 5, 29, 15, 0));
        assertThat(updatedEvent.getLocation()).isEqualTo("会议室 A");
    }

    @Test
    void updateEvent_shouldReturnNotFound_whenEventDoesNotExist() throws Exception {
        Map<String, Object> request = validRequest(
                "不存在会议",
                "合法请求体",
                "2026-05-29T10:00:00",
                "2026-05-29T11:00:00",
                "线上"
        );

        mockMvc.perform(put("/api/calendar/events/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEvent_shouldReturnBadRequest_whenTimeConflictsWithAnotherEvent() throws Exception {
        CalendarEvent event = calendarEventRepository.save(createEvent("待更新会议", 9, 10));
        calendarEventRepository.save(createEvent("已有会议", 11, 12));
        calendarEventRepository.flush();
        Map<String, Object> request = validRequest(
                "冲突更新会议",
                "更新后与已有会议重叠",
                "2026-05-29T11:30:00",
                "2026-05-29T12:30:00",
                "会议室 A"
        );

        mockMvc.perform(put("/api/calendar/events/{id}", event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("日程时间与已有日程冲突"));
    }

    @Test
    void deleteEvent_shouldDeleteEvent_whenEventExists() throws Exception {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("待删除会议", 9, 10));

        mockMvc.perform(delete("/api/calendar/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("删除日历事件成功"));

        assertThat(calendarEventRepository.findById(event.getId())).isEmpty();
    }

    @Test
    void deleteEvent_shouldReturnNotFound_whenEventDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/calendar/events/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    private Map<String, Object> validRequest(
            String title,
            String description,
            String startTime,
            String endTime,
            String location
    ) {
        return Map.of(
                "title", title,
                "description", description,
                "startTime", startTime,
                "endTime", endTime,
                "location", location
        );
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
