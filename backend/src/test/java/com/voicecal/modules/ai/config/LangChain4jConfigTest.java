package com.voicecal.modules.ai.config;

import com.voicecal.modules.ai.service.VoiceCalAssistant;
import com.voicecal.modules.ai.tool.CalendarEventTools;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * LangChain4j Assistant 配置测试。
 */
class LangChain4jConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LangChain4jConfig.class));

    @Test
    void voiceCalAssistant_shouldNotCreateBean_whenApiKeyIsMissing() {
        contextRunner
                .withUserConfiguration(TestConfig.class)
                .run(context -> assertThat(context).doesNotHaveBean(VoiceCalAssistant.class));
    }

    @Test
    void voiceCalAssistant_shouldCreateBean_whenChatModelExistsAndApiKeyIsConfigured() {
        contextRunner
                .withUserConfiguration(TestConfig.class)
                .withPropertyValues("voicecal.ai.qwen.api-key=test-key")
                .run(context -> assertThat(context).hasSingleBean(VoiceCalAssistant.class));
    }

    @Configuration
    static class TestConfig {

        @Bean
        ChatModel chatModel() {
            return mock(ChatModel.class);
        }

        @Bean
        CalendarEventTools calendarEventTools() {
            return mock(CalendarEventTools.class);
        }
    }
}
