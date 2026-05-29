package com.voicecal.modules.ai.service;

import com.voicecal.modules.ai.response.DailySummaryResponse;

/**
 * 每日摘要服务接口。
 */
public interface DailySummaryService {

    /**
     * 生成指定日期的每日摘要。
     *
     * @param date 日期字符串，可为空
     * @param timezone 时区 ID，可为空
     * @return 每日摘要
     */
    DailySummaryResponse getDailySummary(String date, String timezone);
}
