package com.voicecal.modules.log;

import com.voicecal.dao.repository.VoiceCommandLogRepository;
import com.voicecal.modules.log.entity.response.VoiceCommandLogResponse;
import com.voicecal.modules.log.service.VoiceCommandLogService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 语音命令操作日志服务集成测试。
 */
@ActiveProfiles("test")
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class VoiceCommandLogServiceTest {

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
    void saveLog_shouldPersistVoiceCommandLog() {
        VoiceCommandLogResponse response = voiceCommandLogService.saveLog(
                "conversation-a",
                "我明天有什么安排？",
                "你明天有 3 个日程。",
                "CHAT",
                null,
                null,
                null,
                true
        );

        assertThat(response.id()).isNotNull();
        assertThat(response.conversationId()).isEqualTo("conversation-a");
        assertThat(response.rawText()).isEqualTo("我明天有什么安排？");
        assertThat(response.assistantReply()).isEqualTo("你明天有 3 个日程。");
        assertThat(response.success()).isTrue();
        assertThat(response.createdAt()).isNotNull();
        assertThat(voiceCommandLogRepository.findAll()).hasSize(1);
    }

    @Test
    void getRecentLogs_shouldReturnLogsOrderedByCreatedAtDesc() {
        voiceCommandLogService.saveLog("default", "第一条", "回复一", "CHAT", null, null, null, true);
        voiceCommandLogService.saveLog("default", "第二条", "回复二", "CHAT", null, null, null, true);
        voiceCommandLogService.saveLog("default", "第三条", "回复三", "CHAT", null, null, null, true);

        List<VoiceCommandLogResponse> logs = voiceCommandLogService.getRecentLogs(3);

        assertThat(logs).hasSize(3);
        assertThat(logs.get(0).rawText()).isEqualTo("第三条");
        assertThat(logs.get(1).rawText()).isEqualTo("第二条");
        assertThat(logs.get(2).rawText()).isEqualTo("第一条");
    }

    @Test
    void getRecentLogs_shouldRespectLimit() {
        voiceCommandLogService.saveLog("default", "第一条", "回复一", "CHAT", null, null, null, true);
        voiceCommandLogService.saveLog("default", "第二条", "回复二", "CHAT", null, null, null, true);
        voiceCommandLogService.saveLog("default", "第三条", "回复三", "CHAT", null, null, null, true);

        List<VoiceCommandLogResponse> logs = voiceCommandLogService.getRecentLogs(2);

        assertThat(logs).hasSize(2);
    }

    @Test
    void getRecentLogs_shouldUseDefaultLimit() {
        for (int index = 1; index <= 25; index += 1) {
            voiceCommandLogService.saveLog("default", "第 " + index + " 条", "回复", "CHAT", null, null, null, true);
        }

        List<VoiceCommandLogResponse> logs = voiceCommandLogService.getRecentLogs(null);

        assertThat(logs).hasSize(20);
    }

    @Test
    void saveLog_shouldUseDefaultConversationId_whenConversationIdIsBlank() {
        VoiceCommandLogResponse response = voiceCommandLogService.saveLog(
                " ",
                "确认",
                "已确认。",
                "CHAT",
                null,
                null,
                null,
                true
        );

        assertThat(response.conversationId()).isEqualTo("default");
    }
}
