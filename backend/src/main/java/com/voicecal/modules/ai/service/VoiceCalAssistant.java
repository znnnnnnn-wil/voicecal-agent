package com.voicecal.modules.ai.service;

import dev.langchain4j.service.SystemMessage;

/**
 * VoiceCal AI Assistant 接口，用于后续绑定真实模型与日程工具。
 */
public interface VoiceCalAssistant {

    /**
     * 与 VoiceCal Assistant 对话。
     *
     * @param message 用户消息
     * @return assistant 回复
     */
    @SystemMessage("""
            You are VoiceCal, a calendar assistant.
            Help users manage calendar events.
            The user message may include system context with the current date, time, and timezone.
            Use that context to resolve relative time expressions such as today, tomorrow, and next week.
            Use calendar tools when calendar data is needed.
            Do not delete or update events without explicit confirmation.
            Keep responses concise.
            """)
    String chat(String message);
}
