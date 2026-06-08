package com.voicecal.modules.ai.controller;

import com.voicecal.common.response.ApiResponse;
import com.voicecal.modules.ai.entity.request.AiChatRequest;
import com.voicecal.modules.ai.entity.response.AiChatResponse;
import com.voicecal.modules.ai.service.AiChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 对话接口控制器。
 */
@RestController
@RequestMapping("/api/ai/chat")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * 发送用户消息并返回 AI 回复。
     *
     * @param request AI 对话请求
     * @return AI 对话响应
     */
    @PostMapping
    public ApiResponse<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return ApiResponse.success("AI 对话处理成功", aiChatService.chat(request));
    }
}
