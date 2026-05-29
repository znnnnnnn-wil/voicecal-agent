package com.voicecal.modules.reminder.service.impl;

import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.dao.entity.CalendarEvent;
import com.voicecal.dao.repository.CalendarEventRepository;
import com.voicecal.modules.reminder.response.ReminderResponse;
import com.voicecal.modules.reminder.service.ReminderService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 日程提醒服务实现。
 */
@Service
public class ReminderServiceImpl implements ReminderService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final CalendarEventRepository calendarEventRepository;
    private final Clock clock;

    public ReminderServiceImpl(CalendarEventRepository calendarEventRepository, Clock clock) {
        this.calendarEventRepository = calendarEventRepository;
        this.clock = clock;
    }

    /**
     * 扫描并触发已经到达提醒时间的日程。
     *
     * @return 本次触发数量
     */
    @Override
    @Transactional
    public int triggerDueReminders() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<CalendarEvent> candidates =
                calendarEventRepository.findByStatusAndReminderMinutesIsNotNullAndReminderTriggeredFalseAndStartTimeAfterOrderByStartTimeAsc(
                        EventStatus.ACTIVE,
                        now
                );

        int triggeredCount = 0;
        for (CalendarEvent event : candidates) {
            if (shouldTrigger(event, now)) {
                event.setReminderTriggered(true);
                event.setRemindedAt(now);
                triggeredCount++;
            }
        }
        return triggeredCount;
    }

    /**
     * 查询最近已经触发的提醒。
     *
     * @param limit 返回数量限制
     * @return 已触发提醒列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReminderResponse> getRecentTriggeredReminders(Integer limit) {
        int safeLimit = normalizeLimit(limit);
        return calendarEventRepository
                .findByStatusAndReminderTriggeredTrueAndRemindedAtIsNotNullOrderByRemindedAtDesc(
                        EventStatus.ACTIVE,
                        PageRequest.of(0, safeLimit)
                )
                .stream()
                .map(ReminderResponse::from)
                .toList();
    }

    private boolean shouldTrigger(CalendarEvent event, LocalDateTime now) {
        Integer reminderMinutes = event.getReminderMinutes();
        if (reminderMinutes == null || Boolean.TRUE.equals(event.getReminderTriggered())) {
            return false;
        }

        LocalDateTime reminderTime = event.getStartTime().minusMinutes(reminderMinutes);
        return !now.isBefore(reminderTime) && now.isBefore(event.getStartTime());
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1) {
            return 1;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
