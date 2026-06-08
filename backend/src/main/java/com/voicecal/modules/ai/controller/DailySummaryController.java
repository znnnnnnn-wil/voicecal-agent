package com.voicecal.modules.ai.controller;

import com.voicecal.common.response.ApiResponse;
import com.voicecal.modules.ai.entity.response.DailySummaryResponse;
import com.voicecal.modules.ai.service.DailySummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 每日摘要接口控制器。
 */
@RestController
@RequestMapping("/api/ai/daily-summary")
public class DailySummaryController {

    private final DailySummaryService dailySummaryService;

    public DailySummaryController(DailySummaryService dailySummaryService) {
        this.dailySummaryService = dailySummaryService;
    }

    /**
     * 查询指定日期的每日摘要。
     *
     * @param date 日期字符串，格式 yyyy-MM-dd，可为空
     * @param timezone 时区 ID，可为空
     * @return 每日摘要
     */
    @GetMapping
    public ApiResponse<DailySummaryResponse> getDailySummary(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String timezone
    ) {
        return ApiResponse.success("查询每日摘要成功", dailySummaryService.getDailySummary(date, timezone));
    }
}
