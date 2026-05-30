package com.voicecal.modules.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voicecal.dao.repository.CalendarEventRepository;
import com.voicecal.modules.log.repository.VoiceCommandLogRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AI 对话接口集成测试。
 */
@ActiveProfiles("h2")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "voicecal.ai.qwen.api-key="
})
class AiChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private VoiceCommandLogRepository voiceCommandLogRepository;

    @BeforeEach
    void setUp() {
        voiceCommandLogRepository.deleteAll();
        voiceCommandLogRepository.flush();
        calendarEventRepository.deleteAll();
        calendarEventRepository.flush();
    }

    @Test
    void chat_shouldReturnFallbackReply_whenRequestIsValidAndAiProviderIsNotConfigured() throws Exception {
        Map<String, Object> request = Map.of("message", "What is on my calendar?");

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("AI 对话处理成功"))
                .andExpect(jsonPath("$.data.reply").value(
                        "AI provider is not configured yet. VoiceCal calendar tools are registered "
                                + "and ready for use when a chat model is configured."
                ));

        assertVoiceCommandLog("What is on my calendar?", "default");
    }

    @Test
    void chat_shouldCreateLogWithConversationId_whenRequestSucceeds() throws Exception {
        Map<String, Object> request = Map.of(
                "message", "我明天有什么安排？",
                "conversationId", "demo"
        );

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertVoiceCommandLog("我明天有什么安排？", "demo");
    }

    @Test
    void chat_shouldReturnBadRequest_whenMessageIsBlank() throws Exception {
        Map<String, Object> request = Map.of("message", "   ");

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("A0430"));
    }

    private void assertVoiceCommandLog(String rawText, String conversationId) {
        assertThat(voiceCommandLogRepository.findAll()).hasSize(1);
        var log = voiceCommandLogRepository.findAll().get(0);
        assertThat(log.getRawText()).isEqualTo(rawText);
        assertThat(log.getConversationId()).isEqualTo(conversationId);
        assertThat(log.getAssistantReply()).isNotBlank();
        assertThat(log.getSuccess()).isTrue();
    }
}
