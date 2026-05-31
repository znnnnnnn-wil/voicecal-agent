package com.voicecal.modules.log;

import com.voicecal.modules.log.repository.VoiceCommandLogRepository;
import com.voicecal.modules.log.service.VoiceCommandLogService;
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
 * 语音命令操作日志接口集成测试。
 */
@ActiveProfiles("mysql")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class VoiceCommandLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VoiceCommandLogService voiceCommandLogService;

    @Autowired
    private VoiceCommandLogRepository voiceCommandLogRepository;

    @BeforeEach
    void setUp() {
        voiceCommandLogRepository.deleteAll();
        voiceCommandLogRepository.flush();
    }

    @Test
    void getRecentLogs_shouldRespectLimit() throws Exception {
        voiceCommandLogService.saveLog("default", "第一条", "回复一", "CHAT", null, null, null, true);
        voiceCommandLogService.saveLog("default", "第二条", "回复二", "CHAT", null, null, null, true);
        voiceCommandLogService.saveLog("default", "第三条", "回复三", "CHAT", null, null, null, true);

        mockMvc.perform(get("/api/logs/recent")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].rawText").value("第三条"));
    }

    @Test
    void getRecentLogs_shouldUseDefaultLimit() throws Exception {
        for (int index = 1; index <= 25; index += 1) {
            voiceCommandLogService.saveLog("default", "第 " + index + " 条", "回复", "CHAT", null, null, null, true);
        }

        mockMvc.perform(get("/api/logs/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(20));
    }
}
