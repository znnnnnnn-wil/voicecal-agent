package com.voicecal.modules.ai.service.impl;

import com.voicecal.modules.ai.request.AiChatRequest;
import com.voicecal.modules.ai.response.AiChatResponse;
import com.voicecal.modules.ai.service.AiChatService;
import com.voicecal.modules.ai.service.VoiceCalAssistant;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * AI 对话服务实现，负责在真实模型缺失时提供本地可用的降级响应。
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final String FALLBACK_REPLY = "AI provider is not configured yet. "
            + "VoiceCal calendar tools are registered and ready for use when a chat model is configured.";

    private final ObjectProvider<VoiceCalAssistant> voiceCalAssistantProvider;

    public AiChatServiceImpl(ObjectProvider<VoiceCalAssistant> voiceCalAssistantProvider) {
        this.voiceCalAssistantProvider = voiceCalAssistantProvider;
    }

    /**
     * 处理用户对话消息。
     *
     * @param request 对话请求
     * @return AI 对话响应
     */
    @Override
    public AiChatResponse chat(AiChatRequest request) {
        VoiceCalAssistant assistant = voiceCalAssistantProvider.getIfAvailable();
        if (assistant == null) {
            return new AiChatResponse(FALLBACK_REPLY);
        }
        return new AiChatResponse(assistant.chat(request.message()));
    }
}
