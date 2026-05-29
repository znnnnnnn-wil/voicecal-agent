package com.voicecal.modules.assistant.pending;

import com.voicecal.modules.calendar.entity.request.CalendarEventUpdateRequest;
import java.util.List;

/**
 * 待确认操作服务接口。
 */
public interface PendingActionService {

    /**
     * 创建待确认删除日程操作。
     *
     * @param conversationId 对话 ID
     * @param eventId 日程 ID
     * @return 待确认操作
     */
    PendingActionResponse createPendingDeleteAction(String conversationId, Long eventId);

    /**
     * 创建待确认更新日程操作。
     *
     * @param conversationId 对话 ID
     * @param eventId 日程 ID
     * @param updatePayload 更新请求
     * @return 待确认操作
     */
    PendingActionResponse createPendingUpdateAction(
            String conversationId,
            Long eventId,
            CalendarEventUpdateRequest updatePayload
    );

    /**
     * 确认并执行待确认操作。
     *
     * @param conversationId 对话 ID
     * @param actionId 操作 ID
     * @return 执行结果文案
     */
    String confirmPendingAction(String conversationId, String actionId);

    /**
     * 取消待确认操作。
     *
     * @param conversationId 对话 ID
     * @param actionId 操作 ID
     * @return 取消结果文案
     */
    String cancelPendingAction(String conversationId, String actionId);

    /**
     * 查询指定对话下的待确认操作。
     *
     * @param conversationId 对话 ID
     * @return 待确认操作列表
     */
    List<PendingActionResponse> listPendingActions(String conversationId);
}
