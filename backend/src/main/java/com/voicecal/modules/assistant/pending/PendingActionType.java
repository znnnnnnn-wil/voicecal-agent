package com.voicecal.modules.assistant.pending;

/**
 * 待确认操作类型。
 */
public enum PendingActionType {
    /**
     * 删除日程。
     */
    DELETE_EVENT,

    /**
     * 更新日程。
     */
    UPDATE_EVENT,

    /**
     * 创建存在冲突的日程，预留扩展类型。
     */
    CREATE_CONFLICT_EVENT
}
