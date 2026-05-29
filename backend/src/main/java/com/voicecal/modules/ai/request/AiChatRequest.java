package com.voicecal.modules.ai.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * AI 对话请求参数。
 *
 * @param message 用户输入消息
 */
public record AiChatRequest(
        @NotBlank(message = "消息不能为空")
        @Size(max = 2000, message = "消息不能超过 2000 个字符")
        String message
) {
}
