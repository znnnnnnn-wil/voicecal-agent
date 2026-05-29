package com.voicecal.modules.reminder.service;

import com.voicecal.modules.reminder.response.ReminderResponse;
import java.util.List;

/**
 * 日程提醒服务。
 */
public interface ReminderService {

    /**
     * 触发当前已经到期的日程提醒。
     *
     * @return 本次触发的提醒数量
     */
    int triggerDueReminders();

    /**
     * 查询最近已触发的提醒。
     *
     * @param limit 返回数量限制
     * @return 最近已触发提醒列表
     */
    List<ReminderResponse> getRecentTriggeredReminders(Integer limit);
}
