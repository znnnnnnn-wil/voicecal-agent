package com.voicecal.modules.ai.service.impl;

import com.voicecal.modules.ai.request.AiChatRequest;
import com.voicecal.modules.ai.service.VoiceCalAssistant;
import com.voicecal.modules.log.service.VoiceCommandLogService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AI 对话服务测试。
 */
class AiChatServiceImplTest {

    @Test
    void chat_shouldSendCurrentDateContextToAssistant() {
        VoiceCalAssistant assistant = mock(VoiceCalAssistant.class);
        VoiceCommandLogService logService = mock(VoiceCommandLogService.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<VoiceCalAssistant> assistantProvider = mock(ObjectProvider.class);
        AiChatServiceImpl aiChatService = new AiChatServiceImpl(assistantProvider, logService);
        when(assistantProvider.getIfAvailable()).thenReturn(assistant);
        when(assistant.chat(anyString())).thenReturn("已创建提醒");

        aiChatService.chat(new AiChatRequest("明天下午三点提醒我提交项目代码", "demo"));

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(assistant).chat(messageCaptor.capture());
        assertThat(messageCaptor.getValue())
                .contains("当前日期时间")
                .contains("明天日期")
                .contains("当前时区：Asia/Shanghai")
                .contains("reminderMinutes 使用 0")
                .contains("明天下午三点提醒我提交项目代码");
    }
}
