package com.voicecal.modules.ai.service;

import com.voicecal.modules.ai.entity.request.AiChatRequest;
import com.voicecal.modules.ai.entity.response.AiChatResponse;

/**
 * AI 对话服务接口。
 */
public interface AiChatService {

    /**
     * 处理用户对话消息。
     *
     * @param request 对话请求
     * @return AI 对话响应
     */
    AiChatResponse chat(AiChatRequest request);
}
