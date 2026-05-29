package com.voicecal.modules.assistant.pending;

import java.time.LocalDateTime;

/**
 * 待确认操作对象。
 *
 * @param id 操作 ID
 * @param conversationId 对话 ID
 * @param actionType 操作类型
 * @param payloadJson 执行操作所需载荷
 * @param targetSummary 操作目标摘要
 * @param expiresAt 过期时间
 * @param createdAt 创建时间
 */
public record PendingAction(
        String id,
        String conversationId,
        PendingActionType actionType,
        String payloadJson,
        String targetSummary,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {

    /**
     * 判断待确认操作是否已过期。
     *
     * @param now 当前时间
     * @return 已过期返回 true
     */
    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }
}
