package com.voicecal.modules.assistant.pending;

import java.util.List;
import java.util.Optional;

/**
 * 待确认操作存储接口。
 */
public interface PendingActionStore {

    /**
     * 保存待确认操作。
     *
     * @param action 待确认操作
     * @return 已保存的待确认操作
     */
    PendingAction save(PendingAction action);

    /**
     * 根据 ID 查询待确认操作。
     *
     * @param id 操作 ID
     * @return 待确认操作
     */
    Optional<PendingAction> findById(String id);

    /**
     * 根据 ID 和对话 ID 查询待确认操作。
     *
     * @param id 操作 ID
     * @param conversationId 对话 ID
     * @return 待确认操作
     */
    Optional<PendingAction> findByIdAndConversationId(String id, String conversationId);

    /**
     * 删除待确认操作。
     *
     * @param id 操作 ID
     */
    void remove(String id);

    /**
     * 查询指定对话下的待确认操作。
     *
     * @param conversationId 对话 ID
     * @return 待确认操作列表
     */
    List<PendingAction> findByConversationId(String conversationId);

    /**
     * 移除已过期操作。
     */
    void removeExpired();

    /**
     * 清空所有待确认操作，主要用于测试隔离。
     */
    void clear();
}
