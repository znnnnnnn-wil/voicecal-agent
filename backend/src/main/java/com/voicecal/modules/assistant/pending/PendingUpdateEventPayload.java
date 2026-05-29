package com.voicecal.modules.assistant.pending;

import com.voicecal.modules.calendar.entity.request.CalendarEventUpdateRequest;

/**
 * 待确认更新日程载荷。
 *
 * @param eventId 日程 ID
 * @param request 更新请求
 */
public record PendingUpdateEventPayload(Long eventId, CalendarEventUpdateRequest request) {
}
