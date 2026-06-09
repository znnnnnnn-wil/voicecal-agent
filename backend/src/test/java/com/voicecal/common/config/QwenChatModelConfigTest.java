package com.voicecal.common.config;

import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Qwen 聊天模型配置测试。
 */
class QwenChatModelConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(QwenChatModelConfig.class));

    @Test
    void qwenChatModel_shouldNotCreateBean_whenApiKeyIsMissing() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(ChatModel.class));
    }

    @Test
    void qwenChatModel_shouldCreateBean_whenApiKeyIsConfigured() {
        contextRunner
                .withPropertyValues(
                        "voicecal.ai.qwen.base-url=https://dashscope.aliyuncs.com/compatible-mode/v1",
                        "voicecal.ai.qwen.api-key=test-key",
                        "voicecal.ai.qwen.model-name=qwen3.7-max",
                        "voicecal.ai.qwen.temperature=0.2",
                        "voicecal.ai.qwen.timeout-seconds=60",
                        "voicecal.ai.qwen.max-retries=1"
                )
                .run(context -> assertThat(context).hasSingleBean(ChatModel.class));
    }
}
