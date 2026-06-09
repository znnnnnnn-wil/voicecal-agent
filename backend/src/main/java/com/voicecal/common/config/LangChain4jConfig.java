package com.voicecal.common.config;

import com.voicecal.modules.ai.service.VoiceCalAssistant;
import com.voicecal.modules.ai.tool.CalendarEventTools;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j 配置，仅在存在聊天模型 Bean 时创建真实 Assistant。
 */
@Configuration
public class LangChain4jConfig {

    /**
     * 创建绑定日程工具的 VoiceCal Assistant。
     *
     * @param chatModel 聊天模型
     * @param calendarEventTools 日程工具
     * @return VoiceCal Assistant
     */
    @Bean
    @ConditionalOnExpression("'${voicecal.ai.qwen.api-key:}'.trim().length() > 0")
    public VoiceCalAssistant voiceCalAssistant(ChatModel chatModel, CalendarEventTools calendarEventTools) {
        return AiServices.builder(VoiceCalAssistant.class)
                .chatModel(chatModel)
                .tools(calendarEventTools)
                .build();
    }
}
