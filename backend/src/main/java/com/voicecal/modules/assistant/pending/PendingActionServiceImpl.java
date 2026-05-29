package com.voicecal.modules.assistant.pending;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voicecal.common.enums.ResultCodeEnum;
import com.voicecal.common.exception.CustomException;
import com.voicecal.modules.calendar.entity.request.CalendarEventUpdateRequest;
import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import com.voicecal.modules.calendar.service.CalendarEventService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * 待确认操作服务实现。
 */
@Service
public class PendingActionServiceImpl implements PendingActionService {

    private static final String DEFAULT_CONVERSATION_ID = "default";
    private static final long DEFAULT_EXPIRE_MINUTES = 10;

    private final PendingActionStore pendingActionStore;
    private final CalendarEventService calendarEventService;
    private final ObjectMapper objectMapper;

    public PendingActionServiceImpl(
            PendingActionStore pendingActionStore,
            CalendarEventService calendarEventService,
            ObjectMapper objectMapper
    ) {
        this.pendingActionStore = pendingActionStore;
        this.calendarEventService = calendarEventService;
        this.objectMapper = objectMapper;
    }

    @Override
    public PendingActionResponse createPendingDeleteAction(String conversationId, Long eventId) {
        CalendarEventResponse event = calendarEventService.getEvent(eventId);
        PendingAction action = createPendingAction(
                resolveConversationId(conversationId),
                PendingActionType.DELETE_EVENT,
                new PendingDeleteEventPayload(eventId),
                "Delete event \"" + event.title() + "\" from " + event.startTime() + " to " + event.endTime()
        );
        return PendingActionResponse.from(pendingActionStore.save(action));
    }

    @Override
    public PendingActionResponse createPendingUpdateAction(
            String conversationId,
            Long eventId,
            CalendarEventUpdateRequest updatePayload
    ) {
        CalendarEventResponse event = calendarEventService.getEvent(eventId);
        PendingAction action = createPendingAction(
                resolveConversationId(conversationId),
                PendingActionType.UPDATE_EVENT,
                new PendingUpdateEventPayload(eventId, updatePayload),
                "Update event \"" + event.title() + "\" from " + event.startTime() + " to " + event.endTime()
        );
        return PendingActionResponse.from(pendingActionStore.save(action));
    }

    @Override
    public String confirmPendingAction(String conversationId, String actionId) {
        PendingAction action = findAction(actionId, resolveConversationId(conversationId));
        if (action.isExpired(LocalDateTime.now())) {
            pendingActionStore.remove(action.id());
            throw CustomException.create(ResultCodeEnum.PENDING_ACTION_EXPIRED, "待确认操作已过期");
        }

        String result = execute(action);
        pendingActionStore.remove(action.id());
        return result;
    }

    @Override
    public String cancelPendingAction(String conversationId, String actionId) {
        PendingAction action = findAction(actionId, resolveConversationId(conversationId));
        pendingActionStore.remove(action.id());
        return "已取消待确认操作：" + action.targetSummary();
    }

    @Override
    public List<PendingActionResponse> listPendingActions(String conversationId) {
        pendingActionStore.removeExpired();
        return pendingActionStore.findByConversationId(resolveConversationId(conversationId))
                .stream()
                .map(PendingActionResponse::from)
                .toList();
    }

    private PendingAction createPendingAction(
            String conversationId,
            PendingActionType actionType,
            Object payload,
            String targetSummary
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new PendingAction(
                UUID.randomUUID().toString(),
                conversationId,
                actionType,
                serializePayload(payload),
                targetSummary,
                now.plusMinutes(DEFAULT_EXPIRE_MINUTES),
                now
        );
    }

    private String execute(PendingAction action) {
        return switch (action.actionType()) {
            case DELETE_EVENT -> executeDelete(action);
            case UPDATE_EVENT -> executeUpdate(action);
            case CREATE_CONFLICT_EVENT -> throw CustomException.create(
                    ResultCodeEnum.PARAMS_ERROR,
                    "暂不支持确认创建冲突日程"
            );
        };
    }

    private String executeDelete(PendingAction action) {
        PendingDeleteEventPayload payload = deserializePayload(action.payloadJson(), PendingDeleteEventPayload.class);
        calendarEventService.deleteEvent(payload.eventId());
        return "已删除日程";
    }

    private String executeUpdate(PendingAction action) {
        PendingUpdateEventPayload payload = deserializePayload(action.payloadJson(), PendingUpdateEventPayload.class);
        CalendarEventResponse event = calendarEventService.updateEvent(payload.eventId(), payload.request());
        return "已更新日程：" + event.title();
    }

    private PendingAction findAction(String actionId, String conversationId) {
        return pendingActionStore.findByIdAndConversationId(actionId, conversationId)
                .orElseThrow(() -> CustomException.create(
                        ResultCodeEnum.PENDING_ACTION_NOT_FOUND,
                        "待确认操作不存在"
                ));
    }

    private String serializePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw CustomException.create(ResultCodeEnum.PARAMS_ERROR, "待确认操作载荷序列化失败");
        }
    }

    private <T> T deserializePayload(String payloadJson, Class<T> payloadClass) {
        try {
            return objectMapper.readValue(payloadJson, payloadClass);
        } catch (JsonProcessingException exception) {
            throw CustomException.create(ResultCodeEnum.PARAMS_ERROR, "待确认操作载荷解析失败");
        }
    }

    private String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return DEFAULT_CONVERSATION_ID;
        }
        return conversationId;
    }
}
