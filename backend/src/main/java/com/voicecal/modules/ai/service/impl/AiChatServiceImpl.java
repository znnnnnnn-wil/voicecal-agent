package com.voicecal.modules.ai.service.impl;

import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.modules.ai.context.AiRequestContext;
import com.voicecal.modules.ai.request.AiChatRequest;
import com.voicecal.modules.ai.response.AiChatResponse;
import com.voicecal.modules.ai.service.AiChatService;
import com.voicecal.modules.ai.service.VoiceCalAssistant;
import com.voicecal.modules.log.service.VoiceCommandLogService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of(CalendarEvent.DEFAULT_TIMEZONE);

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
            String reply;
            if (assistant == null) {
                reply = FALLBACK_REPLY;
            } else {
                AiRequestContext.setUserMessage(request.message());
                try {
                    reply = assistant.chat(buildContextualMessage(request.message()));
                } finally {
                    AiRequestContext.clear();
                }
            }
            saveLogSafely(request, reply, true);
            return new AiChatResponse(reply);
        } catch (RuntimeException exception) {
            saveLogSafely(request, "AI 请求失败：" + exception.getMessage(), false);
            throw exception;
        }
    }

    private String buildContextualMessage(String userMessage) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE_ID);
        LocalDate today = now.toLocalDate();
        LocalDate tomorrow = today.plusDays(1);
        return """
                系统上下文：
                - 当前日期时间：%s
                - 当前日期：%s
                - 明天日期：%s
                - 当前时区：%s
                - 请基于上述日期解析“今天、明天、后天、下周”等相对时间。
                - 如果用户要求“提醒我”且没有说明提前多久提醒，创建日程时 reminderMinutes 使用 0。
                - 如果用户没有说明结束时间，默认结束时间为开始时间后 30 分钟。
                - 删除或修改日程属于危险操作，只能创建待确认操作，不能直接确认执行。
                - 只有当用户原始消息明确只是“确认、确定、yes、confirm”等确认话术时，才允许确认待执行操作。
                - 用户说“删除会议”时，只能匹配标题、描述或分类明确为会议的日程；不要把提醒、任务、提交代码、学习等非会议日程当成会议删除。
                - 如果没有明确匹配的会议，回复用户没有找到匹配会议，并请用户补充标题或时间。

                用户原始消息：
                %s
                """.formatted(now, today, tomorrow, DEFAULT_ZONE_ID, userMessage);
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
