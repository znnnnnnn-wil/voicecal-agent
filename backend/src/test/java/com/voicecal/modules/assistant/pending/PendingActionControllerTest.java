package com.voicecal.modules.assistant.pending;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 待确认操作接口集成测试。
 */
@ActiveProfiles("h2")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PendingActionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void pendingActionEndpoints_shouldCreateListAndConfirmDeleteAction() throws Exception {
        CalendarEvent event = calendarEventRepository.saveAndFlush(createEvent("产品评审", 14, 15));
        Map<String, Object> request = Map.of(
                "conversationId", "conversation-a",
                "eventId", event.getId()
        );

        String response = mockMvc.perform(post("/api/pending-actions/delete-event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.actionType").value("DELETE_EVENT"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String actionId = objectMapper.readTree(response).path("data").path("id").asText();

        mockMvc.perform(get("/api/pending-actions")
                        .param("conversationId", "conversation-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(actionId));

        mockMvc.perform(post("/api/pending-actions/{id}/confirm", actionId)
                        .param("conversationId", "conversation-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("已删除日程"));
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
}
