package com.voicecal.modules.assistant.pending;

import java.time.LocalDateTime;

/**
 * 待确认操作响应对象。
 *
 * @param id 操作 ID
 * @param conversationId 对话 ID
 * @param actionType 操作类型
 * @param targetSummary 操作目标摘要
 * @param expiresAt 过期时间
 * @param createdAt 创建时间
 */
public record PendingActionResponse(
        String id,
        String conversationId,
        PendingActionType actionType,
        String targetSummary,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {

    /**
     * 转换为响应对象。
     *
     * @param action 待确认操作
     * @return 响应对象
     */
    public static PendingActionResponse from(PendingAction action) {
        return new PendingActionResponse(
                action.id(),
                action.conversationId(),
                action.actionType(),
                action.targetSummary(),
                action.expiresAt(),
                action.createdAt()
        );
    }
}
