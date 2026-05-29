package com.voicecal.modules.assistant.pending;

/**
 * 待确认删除日程载荷。
 *
 * @param eventId 日程 ID
 */
public record PendingDeleteEventPayload(Long eventId) {
}
