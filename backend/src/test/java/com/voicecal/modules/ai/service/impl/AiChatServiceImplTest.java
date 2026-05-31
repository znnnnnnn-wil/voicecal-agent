package com.voicecal.modules.ai.service.impl;

import com.voicecal.common.enums.dao.EventCategory;
import com.voicecal.modules.ai.request.AiChatRequest;
import com.voicecal.modules.ai.response.AiChatResponse;
import com.voicecal.modules.ai.service.VoiceCalAssistant;
import com.voicecal.modules.assistant.router.FastCommandRouteResult;
import com.voicecal.modules.assistant.router.FastCommandRouter;
import com.voicecal.modules.assistant.router.FastCommandType;
import com.voicecal.modules.calendar.entity.response.CalendarEventResponse;
import com.voicecal.modules.calendar.service.CalendarAvailabilityService;
import com.voicecal.modules.calendar.service.CalendarEventQueryService;
import com.voicecal.modules.log.response.VoiceCommandLogResponse;
import com.voicecal.modules.log.service.VoiceCommandLogService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * AI 对话服务测试。
 */
class AiChatServiceImplTest {

    @Test
    void chat_shouldReturnFastRuleResponse_whenRouterMatches() {
        VoiceCalAssistant assistant = mock(VoiceCalAssistant.class);
        VoiceCommandLogService logService = mock(VoiceCommandLogService.class);
        FastCommandRouter fastCommandRouter = mock(FastCommandRouter.class);
        CalendarEventQueryService calendarEventQueryService = mock(CalendarEventQueryService.class);
        CalendarAvailabilityService calendarAvailabilityService = mock(CalendarAvailabilityService.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<VoiceCalAssistant> assistantProvider = mock(ObjectProvider.class);
        AiChatServiceImpl aiChatService = new AiChatServiceImpl(
                assistantProvider,
                logService,
                fastCommandRouter,
                calendarEventQueryService,
                calendarAvailabilityService
        );
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        when(fastCommandRouter.tryRoute("我今天有什么安排"))
                .thenReturn(FastCommandRouteResult.matched(FastCommandType.TODAY_EVENTS));
        when(calendarEventQueryService.getEventsForDate(any(java.time.LocalDate.class), any(ZoneId.class)))
                .thenReturn(List.of(new CalendarEventResponse(
                        1L,
                        "项目会",
                        "",
                        startTime,
                        startTime.plusMinutes(30),
                        "",
                        EventCategory.MEETING,
                        0,
                        false,
                        null,
                        startTime.minusDays(1),
                        startTime.minusDays(1)
                )));

        AiChatResponse response = aiChatService.chat(new AiChatRequest("我今天有什么安排", "demo"));

        assertThat(response.routedBy()).isEqualTo("FAST_RULE");
        assertThat(response.reply()).contains("今天的安排").contains("项目会");
        verifyNoInteractions(assistant);
        verify(logService).saveLog(eq("demo"), eq("我今天有什么安排"), anyString(), eq("FAST_RULE"),
                eq(null), eq(null), eq(null), eq(true));
    }

    @Test
    void chat_shouldSendCurrentDateContextToAssistant_whenRouterDoesNotMatch() {
        VoiceCalAssistant assistant = mock(VoiceCalAssistant.class);
        VoiceCommandLogService logService = mock(VoiceCommandLogService.class);
        FastCommandRouter fastCommandRouter = mock(FastCommandRouter.class);
        CalendarEventQueryService calendarEventQueryService = mock(CalendarEventQueryService.class);
        CalendarAvailabilityService calendarAvailabilityService = mock(CalendarAvailabilityService.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<VoiceCalAssistant> assistantProvider = mock(ObjectProvider.class);
        AiChatServiceImpl aiChatService = new AiChatServiceImpl(
                assistantProvider,
                logService,
                fastCommandRouter,
                calendarEventQueryService,
                calendarAvailabilityService
        );
        when(fastCommandRouter.tryRoute("明天下午三点提醒我提交项目代码"))
                .thenReturn(FastCommandRouteResult.notMatched());
        when(assistantProvider.getIfAvailable()).thenReturn(assistant);
        when(assistant.chat(anyString())).thenReturn("已创建提醒");

        AiChatResponse response = aiChatService.chat(new AiChatRequest("明天下午三点提醒我提交项目代码", "demo"));

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(assistant).chat(messageCaptor.capture());
        assertThat(response.routedBy()).isEqualTo("LLM");
        assertThat(messageCaptor.getValue())
                .contains("当前日期时间")
                .contains("明天日期")
                .contains("当前时区：Asia/Shanghai")
                .contains("reminderMinutes 使用 0")
                .contains("明天下午三点提醒我提交项目代码");
    }

    @Test
    void chat_shouldSendRecentConversationContextToAssistant_whenUserRepliesWithClarification() {
        VoiceCalAssistant assistant = mock(VoiceCalAssistant.class);
        VoiceCommandLogService logService = mock(VoiceCommandLogService.class);
        FastCommandRouter fastCommandRouter = mock(FastCommandRouter.class);
        CalendarEventQueryService calendarEventQueryService = mock(CalendarEventQueryService.class);
        CalendarAvailabilityService calendarAvailabilityService = mock(CalendarAvailabilityService.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<VoiceCalAssistant> assistantProvider = mock(ObjectProvider.class);
        AiChatServiceImpl aiChatService = new AiChatServiceImpl(
                assistantProvider,
                logService,
                fastCommandRouter,
                calendarEventQueryService,
                calendarAvailabilityService
        );
        when(fastCommandRouter.tryRoute("凌晨")).thenReturn(FastCommandRouteResult.notMatched());
        when(assistantProvider.getIfAvailable()).thenReturn(assistant);
        when(logService.getRecentLogs("demo", 3)).thenReturn(List.of(new VoiceCommandLogResponse(
                1L,
                "demo",
                "下周一晚上两点要开会",
                "你说的晚上两点是凌晨 2 点还是下午 2 点？",
                "LLM",
                null,
                null,
                null,
                true,
                LocalDateTime.of(2026, 5, 31, 10, 0)
        )));
        when(assistant.chat(anyString())).thenReturn("已按凌晨 2 点创建会议。");

        aiChatService.chat(new AiChatRequest("凌晨", "demo"));

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(assistant).chat(messageCaptor.capture());
        assertThat(messageCaptor.getValue())
                .contains("最近对话上下文")
                .contains("下周一晚上两点要开会")
                .contains("凌晨 2 点还是下午 2 点")
                .contains("用户原始消息")
                .contains("凌晨");
    }
}
