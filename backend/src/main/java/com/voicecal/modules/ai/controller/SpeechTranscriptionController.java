package com.voicecal.modules.ai.controller;

import com.voicecal.common.response.ApiResponse;
import com.voicecal.modules.ai.response.SpeechTranscriptionResponse;
import com.voicecal.modules.ai.service.SpeechTranscriptionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 语音识别接口控制器。
 */
@RestController
@RequestMapping("/api/ai/speech")
public class SpeechTranscriptionController {

    private final SpeechTranscriptionService speechTranscriptionService;

    public SpeechTranscriptionController(SpeechTranscriptionService speechTranscriptionService) {
        this.speechTranscriptionService = speechTranscriptionService;
    }

    /**
     * 上传音频并返回识别文本。
     *
     * @param audio 音频文件
     * @return 语音识别结果
     */
    @PostMapping("/transcriptions")
    public ApiResponse<SpeechTranscriptionResponse> transcribe(@RequestParam("audio") MultipartFile audio) {
        return ApiResponse.success("语音识别成功", speechTranscriptionService.transcribe(audio));
    }
}
