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
import com.voicecal.modules.log.response.VoiceCommandLogResponse;
import com.voicecal.modules.log.service.VoiceCommandLogService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
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
    private static final String DEFAULT_CONVERSATION_ID = "default";
    private static final int CONTEXT_LOG_LIMIT = 5;
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
                    reply = assistant.chat(buildContextualMessage(request));
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
        LocalTime start = resolveFreeTimeStart(message);
        LocalTime end = resolveFreeTimeEnd(message);
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
        if (message.contains("后天")) {
            return today.plusDays(2);
        }
        if (message.contains("明天")) {
            return today.plusDays(1);
        }
        DayOfWeek dayOfWeek = resolveDayOfWeek(message);
        if (dayOfWeek != null) {
            if (message.contains("下周")) {
                return today.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).with(TemporalAdjusters.nextOrSame(dayOfWeek));
            }
            return today.with(TemporalAdjusters.nextOrSame(dayOfWeek));
        }
        return today;
    }

    private DayOfWeek resolveDayOfWeek(String message) {
        if (message.contains("周一") || message.contains("星期一")) {
            return DayOfWeek.MONDAY;
        }
        if (message.contains("周二") || message.contains("星期二")) {
            return DayOfWeek.TUESDAY;
        }
        if (message.contains("周三") || message.contains("星期三")) {
            return DayOfWeek.WEDNESDAY;
        }
        if (message.contains("周四") || message.contains("星期四")) {
            return DayOfWeek.THURSDAY;
        }
        if (message.contains("周五") || message.contains("星期五")) {
            return DayOfWeek.FRIDAY;
        }
        if (message.contains("周六") || message.contains("星期六")) {
            return DayOfWeek.SATURDAY;
        }
        if (message.contains("周日") || message.contains("周天") || message.contains("星期日") || message.contains("星期天")) {
            return DayOfWeek.SUNDAY;
        }
        return null;
    }

    private LocalTime resolveFreeTimeStart(String message) {
        if (message.contains("上午")) {
            return LocalTime.of(9, 0);
        }
        if (message.contains("中午")) {
            return LocalTime.of(12, 0);
        }
        if (message.contains("晚上") || message.contains("今晚")) {
            return LocalTime.of(18, 0);
        }
        if (message.contains("白天")) {
            return LocalTime.of(9, 0);
        }
        return LocalTime.of(13, 0);
    }

    private LocalTime resolveFreeTimeEnd(String message) {
        if (message.contains("上午")) {
            return LocalTime.of(12, 0);
        }
        if (message.contains("中午")) {
            return LocalTime.of(14, 0);
        }
        if (message.contains("晚上") || message.contains("今晚")) {
            return LocalTime.of(22, 0);
        }
        if (message.contains("白天")) {
            return LocalTime.of(18, 0);
        }
        return LocalTime.of(18, 0);
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

    private String buildContextualMessage(AiChatRequest request) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE_ID);
        LocalDate today = now.toLocalDate();
        LocalDate tomorrow = today.plusDays(1);
        String conversationContext = buildConversationContext(request.conversationId());
        return """
                系统上下文：
                - 当前日期时间：%s
                - 当前日期：%s
                - 明天日期：%s
                - 当前时区：%s
                - 请基于上述日期解析“今天、明天、后天、下周”等相对时间。
                - 中文下午时间必须按 24 小时制正确换算：“下午一点”是 13:00，“下午两点”是 14:00，“下午三点”是 15:00，“下午四点”是 16:00，以此类推到“下午十一点”是 23:00。
                - 用户说“明天下午四点提醒我睡觉”时，startTime 必须是明天 16:00，不是 15:00。
                - 如果用户要求“提醒我”且没有说明提前多久提醒，创建日程时 reminderMinutes 使用 0。
                - 如果用户给出明确未来时间和行动事项，例如“七点起床”“明天五点写作业”“八点开会”，即使没说“提醒我”，也按到点提醒事项处理，创建日程时 reminderMinutes 使用 0。
                - 如果用户没有说明结束时间或持续时间，不要默认 30 分钟；创建日程时 endTime 使用和 startTime 相同的时间，表示这是一个时间点事项。
                - 删除、修改日程属于重要操作，不能在用户首次提出时直接执行；必须先列出将要删除或修改的目标日程，并询问用户是否确认。
                - 只有当用户在最近上下文后明确回复“确认、确定、是的、执行、删除吧、修改吧”等确认语义时，才可以调用删除或修改工具。
                - 如果用户要求按时间范围导出 ICS，请先把时间范围解析为 ISO-8601 LocalDateTime，再调用 ICS 导出工具。
                - ICS 导出工具返回下载链接后，请把链接原样回复给用户，方便前端渲染下载入口。
                - 用户说“删除会议”时，只能匹配标题、描述或分类明确为会议的日程；标题或描述包含“会议、开会、晨会、例会、周会、评审、meeting、review、sync、standup”都应视为会议。
                - 不要把提醒、任务、提交代码、学习、看展览等非会议日程当成会议删除。
                - 如果没有明确匹配的会议，回复用户没有找到匹配会议，并请用户补充标题或时间。
                - 如果用户只回复“凌晨”“下午”“是的”“确认”等简短信息，必须结合下方最近对话上下文理解，不要把它当成全新指令。

                最近对话上下文：
                %s

                用户原始消息：
                %s
                """.formatted(now, today, tomorrow, DEFAULT_ZONE_ID, conversationContext, request.message());
    }

    private String buildConversationContext(String conversationId) {
        List<VoiceCommandLogResponse> logs;
        try {
            logs = voiceCommandLogService.getRecentLogs(resolveConversationId(conversationId), CONTEXT_LOG_LIMIT);
        } catch (RuntimeException exception) {
            return "无";
        }
        if (logs == null || logs.isEmpty()) {
            return "无";
        }
        return logs.stream()
                .sorted(Comparator.comparing(VoiceCommandLogResponse::createdAt))
                .map(log -> "- 用户：" + log.rawText() + "\n  助手：" + nullToEmpty(log.assistantReply()))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("无");
    }

    private String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return DEFAULT_CONVERSATION_ID;
        }
        return conversationId;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
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
