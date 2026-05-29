package com.voicecal.modules.assistant.pending;

import com.voicecal.modules.calendar.entity.request.CalendarEventUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 创建待确认更新操作请求。
 *
 * @param conversationId 对话 ID
 * @param eventId 日程 ID
 * @param updateRequest 更新请求
 */
public record CreatePendingUpdateActionRequest(
        String conversationId,

        @NotNull(message = "日程 ID 不能为空")
        Long eventId,

        @Valid
        @NotNull(message = "更新请求不能为空")
        CalendarEventUpdateRequest updateRequest
) {
}
