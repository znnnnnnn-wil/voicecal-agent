package com.voicecal.modules.ai.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Qwen 聊天模型配置，通过 DashScope 的 OpenAI 兼容接口接入真实大模型。
 */
@Configuration
public class QwenChatModelConfig {

    /**
     * 创建 Qwen ChatModel。未配置密钥时不会创建 Bean，系统继续使用本地降级响应。
     *
     * @param baseUrl OpenAI 兼容接口地址
     * @param apiKey DashScope API Key
     * @param modelName 模型名称
     * @param temperature 生成温度
     * @param timeoutSeconds 请求超时时间，单位秒
     * @param maxRetries 最大重试次数
     * @return LangChain4j 聊天模型
     */
    @Bean
    @ConditionalOnExpression("'${voicecal.ai.qwen.api-key:}'.trim().length() > 0")
    public ChatModel qwenChatModel(
            @Value("${voicecal.ai.qwen.base-url}") String baseUrl,
            @Value("${voicecal.ai.qwen.api-key}") String apiKey,
            @Value("${voicecal.ai.qwen.model-name}") String modelName,
            @Value("${voicecal.ai.qwen.temperature}") Double temperature,
            @Value("${voicecal.ai.qwen.timeout-seconds}") Long timeoutSeconds,
            @Value("${voicecal.ai.qwen.max-retries}") Integer maxRetries
    ) {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .maxRetries(maxRetries)
                .logRequests(false)
                .logResponses(false)
                .build();
    }
}
