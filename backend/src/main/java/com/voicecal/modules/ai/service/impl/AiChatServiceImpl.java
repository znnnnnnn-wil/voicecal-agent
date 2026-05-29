package com.voicecal.modules.ai.service.impl;

import com.voicecal.modules.ai.request.AiChatRequest;
import com.voicecal.modules.ai.response.AiChatResponse;
import com.voicecal.modules.ai.service.AiChatService;
import com.voicecal.modules.ai.service.VoiceCalAssistant;
import com.voicecal.modules.log.service.VoiceCommandLogService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * AI 对话服务实现，负责在真实模型缺失时提供本地可用的降级响应。
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final String FALLBACK_REPLY = "AI provider is not configured yet. "
            + "VoiceCal calendar tools are registered and ready for use when a chat model is configured.";
    private static final String INTENT_CHAT = "CHAT";

    private final ObjectProvider<VoiceCalAssistant> voiceCalAssistantProvider;
    private final VoiceCommandLogService voiceCommandLogService;

    public AiChatServiceImpl(
            ObjectProvider<VoiceCalAssistant> voiceCalAssistantProvider,
            VoiceCommandLogService voiceCommandLogService
    ) {
        this.voiceCalAssistantProvider = voiceCalAssistantProvider;
        this.voiceCommandLogService = voiceCommandLogService;
    }

    /**
     * 处理用户对话消息。
     *
     * @param request 对话请求
     * @return AI 对话响应
     */
    @Override
    public AiChatResponse chat(AiChatRequest request) {
        try {
            VoiceCalAssistant assistant = voiceCalAssistantProvider.getIfAvailable();
            String reply = assistant == null ? FALLBACK_REPLY : assistant.chat(request.message());
            saveLogSafely(request, reply, true);
            return new AiChatResponse(reply);
        } catch (RuntimeException exception) {
            saveLogSafely(request, "AI 请求失败：" + exception.getMessage(), false);
            throw exception;
        }
    }

    private void saveLogSafely(AiChatRequest request, String assistantReply, boolean success) {
        try {
            voiceCommandLogService.saveLog(
                    request.conversationId(),
                    request.message(),
                    assistantReply,
                    INTENT_CHAT,
                    null,
                    null,
                    null,
                    success
            );
        } catch (RuntimeException ignored) {
            // 日志写入失败不能影响 AI 对话主流程。
        }
    }
}
