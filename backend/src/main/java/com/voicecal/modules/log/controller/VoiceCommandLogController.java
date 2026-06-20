package com.voicecal.modules.log.controller;

import com.voicecal.common.response.ApiResponse;
import com.voicecal.modules.log.entity.response.VoiceCommandLogResponse;
import com.voicecal.modules.log.service.VoiceCommandLogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 语音命令操作日志接口控制器。
 */
@RestController
@RequestMapping("/api/logs")
public class VoiceCommandLogController {

    private final VoiceCommandLogService voiceCommandLogService;

    public VoiceCommandLogController(VoiceCommandLogService voiceCommandLogService) {
        this.voiceCommandLogService = voiceCommandLogService;
    }

    /**
     * 查询最近语音命令操作日志。
     *
     * @param limit 数量限制，可为空
     * @return 最近日志列表
     */
    @GetMapping("/recent")
    public ApiResponse<List<VoiceCommandLogResponse>> getRecentLogs(
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.success("查询最近操作日志成功", voiceCommandLogService.getRecentLogs(limit));
    }
}
