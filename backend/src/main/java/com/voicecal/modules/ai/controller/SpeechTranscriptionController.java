package com.voicecal.modules.ai.controller;

import com.voicecal.common.response.ApiResponse;
import com.voicecal.modules.ai.entity.response.SpeechTranscriptionResponse;
import com.voicecal.modules.ai.service.SpeechTranscriptionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 语音转写兼容入口，面向前端语音链路。
 */
@RestController
@RequestMapping("/api/voice")
public class SpeechTranscriptionController {

    private final SpeechTranscriptionService speechTranscriptionService;

    public SpeechTranscriptionController(SpeechTranscriptionService speechTranscriptionService) {
        this.speechTranscriptionService = speechTranscriptionService;
    }

    /**
     * 上传音频并返回识别文本。
     *
     * @param audio 音频文件
     * @return 识别结果
     */
    @PostMapping("/transcribe")
    public ApiResponse<SpeechTranscriptionResponse> transcribe(
            @RequestParam("audio") MultipartFile audio
    ) {
        return ApiResponse.success("识别成功", speechTranscriptionService.transcribe(audio));
    }
}
