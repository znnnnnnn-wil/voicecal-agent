package com.voicecal.modules.reminder;

import com.voicecal.modules.reminder.service.ReminderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 日程提醒定时扫描任务。
 */
@Component
public class ReminderScheduler {

    private final ReminderService reminderService;

    public ReminderScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * 定时触发到期提醒。MVP 只记录提醒状态，不做真实推送。
     */
    @Scheduled(
            fixedDelayString = "${voicecal.reminder.scan-delay-ms:60000}",
            initialDelayString = "${voicecal.reminder.initial-delay-ms:60000}"
    )
    public void scanDueReminders() {
        reminderService.triggerDueReminders();
    }
}
