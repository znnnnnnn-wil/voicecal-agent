package com.voicecal.modules.assistant.pending;

import jakarta.validation.constraints.NotNull;

/**
 * 创建待确认删除操作请求。
 *
 * @param conversationId 对话 ID
 * @param eventId 日程 ID
 */
public record CreatePendingDeleteActionRequest(
        String conversationId,

        @NotNull(message = "日程 ID 不能为空")
        Long eventId
) {
}
