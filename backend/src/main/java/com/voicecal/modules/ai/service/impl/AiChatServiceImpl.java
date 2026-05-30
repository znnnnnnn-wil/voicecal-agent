package com.voicecal.modules.ai.service.impl;

import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.modules.ai.context.AiRequestContext;
import com.voicecal.modules.ai.request.AiChatRequest;
import com.voicecal.modules.ai.response.AiChatResponse;
import com.voicecal.modules.ai.service.AiChatService;
import com.voicecal.modules.ai.service.VoiceCalAssistant;
import com.voicecal.modules.assistant.router.FastCommandRouteResult;
import com.voicecal.modules.assistant.router.FastCommandRouter;
import com.voicecal.modules.assistant.router.FastCommandType;
import com.voicecal.modules.calendar.entity.request.FreeTimeQueryRequest;
import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import com.voicecal.modules.calendar.entity.response.FreeTimeSlotResponse;
import com.voicecal.modules.calendar.service.CalendarAvailabilityService;
import com.voicecal.modules.calendar.service.CalendarEventQueryService;
import com.voicecal.modules.log.service.VoiceCommandLogService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.time.temporal.TemporalAdjusters;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * AI 对话服务实现，先尝试安全查询类快速路由，再回退到 LangChain4j Agent。
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final String FALLBACK_REPLY = "AI provider is not configured yet. "
            + "VoiceCal calendar tools are registered and ready for use when a chat model is configured.";
    private static final String ROUTED_BY_FAST_RULE = "FAST_RULE";
    private static final String ROUTED_BY_LLM = "LLM";
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of(CalendarEvent.DEFAULT_TIMEZONE);

    private final ObjectProvider<VoiceCalAssistant> voiceCalAssistantProvider;
    private final VoiceCommandLogService voiceCommandLogService;
    private final FastCommandRouter fastCommandRouter;
    private final CalendarEventQueryService calendarEventQueryService;
    private final CalendarAvailabilityService calendarAvailabilityService;

    public AiChatServiceImpl(
            ObjectProvider<VoiceCalAssistant> voiceCalAssistantProvider,
            VoiceCommandLogService voiceCommandLogService,
            FastCommandRouter fastCommandRouter,
            CalendarEventQueryService calendarEventQueryService,
            CalendarAvailabilityService calendarAvailabilityService
    ) {
        this.voiceCalAssistantProvider = voiceCalAssistantProvider;
        this.voiceCommandLogService = voiceCommandLogService;
        this.fastCommandRouter = fastCommandRouter;
        this.calendarEventQueryService = calendarEventQueryService;
        this.calendarAvailabilityService = calendarAvailabilityService;
    }

    /**
     * 处理用户对话消息。
     *
     * @param request 对话请求
     * @return AI 对话响应
     */
    @Override
    public AiChatResponse chat(AiChatRequest request) {
        try {
            FastCommandRouteResult routeResult = fastCommandRouter.tryRoute(request.message());
            if (routeResult.matched()) {
                String reply = handleFastRoute(routeResult.type(), request.message());
                saveLogSafely(request, reply, true, ROUTED_BY_FAST_RULE);
                return new AiChatResponse(reply, ROUTED_BY_FAST_RULE);
            }

            VoiceCalAssistant assistant = voiceCalAssistantProvider.getIfAvailable();
            String reply;
            if (assistant == null) {
                reply = FALLBACK_REPLY;
            } else {
                AiRequestContext.setUserMessage(request.message());
                try {
                    reply = assistant.chat(buildContextualMessage(request.message()));
                } finally {
                    AiRequestContext.clear();
                }
            }
            saveLogSafely(request, reply, true, ROUTED_BY_LLM);
            return new AiChatResponse(reply, ROUTED_BY_LLM);
        } catch (RuntimeException exception) {
            saveLogSafely(request, "AI 请求失败：" + exception.getMessage(), false, ROUTED_BY_LLM);
            throw exception;
        }
    }

    private String handleFastRoute(FastCommandType type, String message) {
        LocalDate today = LocalDate.now(DEFAULT_ZONE_ID);
        return switch (type) {
            case TODAY_EVENTS -> formatEvents("今天的安排", calendarEventQueryService.getEventsForDate(today, DEFAULT_ZONE_ID));
            case TOMORROW_EVENTS -> formatEvents("明天的安排", calendarEventQueryService.getEventsForDate(today.plusDays(1), DEFAULT_ZONE_ID));
            case WEEK_EVENTS -> formatEvents("本周日程", calendarEventQueryService.getCurrentWeekEvents(DEFAULT_ZONE_ID.getId()));
            case FREE_TIME -> formatFreeTime(message, today);
        };
    }

    private String formatFreeTime(String message, LocalDate today) {
        LocalDate date = resolveFreeTimeDate(message, today);
        LocalTime start = message.contains("晚上") || message.contains("今晚") ? LocalTime.of(18, 0) : LocalTime.of(13, 0);
        LocalTime end = message.contains("晚上") || message.contains("今晚") ? LocalTime.of(22, 0) : LocalTime.of(18, 0);
        List<FreeTimeSlotResponse> slots = calendarAvailabilityService.findFreeTimeSlots(
                new FreeTimeQueryRequest(date.atTime(start), date.atTime(end), 30)
        );
        if (slots.isEmpty()) {
            return "这段时间没有满足条件的空闲时间。";
        }
        return "可用空闲时间：" + slots.stream()
                .map(slot -> slot.startTime().toLocalTime() + "-" + slot.endTime().toLocalTime())
                .reduce((left, right) -> left + "；" + right)
                .orElse("");
    }

    private LocalDate resolveFreeTimeDate(String message, LocalDate today) {
        if (message.contains("明天")) {
            return today.plusDays(1);
        }
        if (message.contains("周五") || message.contains("星期五")) {
            return today.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        }
        return today;
    }

    private String formatEvents(String title, List<CalendarEventResponse> events) {
        if (events.isEmpty()) {
            return title + "：暂无日程。";
        }
        return title + "：" + events.stream()
                .map(event -> event.startTime().toLocalTime() + " " + event.title())
                .reduce((left, right) -> left + "；" + right)
                .orElse("");
    }

    private String buildContextualMessage(String userMessage) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE_ID);
        LocalDate today = now.toLocalDate();
        LocalDate tomorrow = today.plusDays(1);
        return """
                系统上下文：
                - 当前日期时间：%s
                - 当前日期：%s
                - 明天日期：%s
                - 当前时区：%s
                - 请基于上述日期解析“今天、明天、后天、下周”等相对时间。
                - 如果用户要求“提醒我”且没有说明提前多久提醒，创建日程时 reminderMinutes 使用 0。
                - 如果用户没有说明结束时间，默认结束时间为开始时间后 30 分钟。
                - 如果用户明确要求删除或修改日程，直接调用删除或修改工具执行，不要要求二次确认。
                - 用户说“删除会议”时，只能匹配标题、描述或分类明确为会议的日程；不要把提醒、任务、提交代码、学习等非会议日程当成会议删除。
                - 如果没有明确匹配的会议，回复用户没有找到匹配会议，并请用户补充标题或时间。

                用户原始消息：
                %s
                """.formatted(now, today, tomorrow, DEFAULT_ZONE_ID, userMessage);
    }

    private void saveLogSafely(AiChatRequest request, String assistantReply, boolean success, String intent) {
        try {
            voiceCommandLogService.saveLog(
                    request.conversationId(),
                    request.message(),
                    assistantReply,
                    intent,
                    null,
                    null,
                    null,
                    success
            );
        } catch (RuntimeException ignored) {
            // 日志写入失败不能影响 AI 对话主流程。
        }
    }
}
