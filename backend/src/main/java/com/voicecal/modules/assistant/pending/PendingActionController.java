package com.voicecal.modules.assistant.pending;

import com.voicecal.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 待确认操作接口控制器。
 */
@RestController
@RequestMapping("/api/pending-actions")
public class PendingActionController {

    private final PendingActionService pendingActionService;

    public PendingActionController(PendingActionService pendingActionService) {
        this.pendingActionService = pendingActionService;
    }

    /**
     * 创建待确认删除日程操作。
     *
     * @param request 创建请求
     * @return 待确认操作
     */
    @PostMapping("/delete-event")
    public ApiResponse<PendingActionResponse> createPendingDeleteAction(
            @Valid @RequestBody CreatePendingDeleteActionRequest request
    ) {
        return ApiResponse.success(
                "创建待确认删除操作成功",
                pendingActionService.createPendingDeleteAction(request.conversationId(), request.eventId())
        );
    }

    /**
     * 创建待确认更新日程操作。
     *
     * @param request 创建请求
     * @return 待确认操作
     */
    @PostMapping("/update-event")
    public ApiResponse<PendingActionResponse> createPendingUpdateAction(
            @Valid @RequestBody CreatePendingUpdateActionRequest request
    ) {
        return ApiResponse.success(
                "创建待确认更新操作成功",
                pendingActionService.createPendingUpdateAction(
                        request.conversationId(),
                        request.eventId(),
                        request.updateRequest()
                )
        );
    }

    /**
     * 查询指定对话下的待确认操作。
     *
     * @param conversationId 对话 ID
     * @return 待确认操作列表
     */
    @GetMapping
    public ApiResponse<List<PendingActionResponse>> listPendingActions(
            @RequestParam(defaultValue = "default") String conversationId
    ) {
        return ApiResponse.success("查询待确认操作成功", pendingActionService.listPendingActions(conversationId));
    }

    /**
     * 确认并执行待确认操作。
     *
     * @param id 操作 ID
     * @param conversationId 对话 ID
     * @return 执行结果
     */
    @PostMapping("/{id}/confirm")
    public ApiResponse<String> confirmPendingAction(
            @PathVariable String id,
            @RequestParam(defaultValue = "default") String conversationId
    ) {
        return ApiResponse.success("确认待确认操作成功", pendingActionService.confirmPendingAction(conversationId, id));
    }

    /**
     * 取消待确认操作。
     *
     * @param id 操作 ID
     * @param conversationId 对话 ID
     * @return 取消结果
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<String> cancelPendingAction(
            @PathVariable String id,
            @RequestParam(defaultValue = "default") String conversationId
    ) {
        return ApiResponse.success("取消待确认操作成功", pendingActionService.cancelPendingAction(conversationId, id));
    }
}
