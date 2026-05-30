package com.voicecal.modules.reminder.controller;

import com.voicecal.common.response.ApiResponse;
import com.voicecal.modules.reminder.response.ReminderResponse;
import com.voicecal.modules.reminder.service.ReminderService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日程提醒接口。
 */
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * 查询最近已触发提醒。
     *
     * @param limit 返回数量限制
     * @return 最近已触发提醒
     */
    @GetMapping("/recent")
    public ApiResponse<List<ReminderResponse>> getRecentReminders(
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        reminderService.triggerDueReminders();
        return ApiResponse.success("查询最近提醒成功", reminderService.getRecentTriggeredReminders(limit));
    }
}
