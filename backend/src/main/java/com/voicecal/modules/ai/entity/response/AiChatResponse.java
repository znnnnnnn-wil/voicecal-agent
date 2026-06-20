package com.voicecal.modules.ai.entity.response;

/**
 * AI 对话响应。
 *
 * @param reply 回复文本
 * @param routedBy 路由来源，FAST_RULE 或 LLM
 */
public record AiChatResponse(String reply, String routedBy) {

    public AiChatResponse(String reply) {
        this(reply, "LLM");
    }
}
