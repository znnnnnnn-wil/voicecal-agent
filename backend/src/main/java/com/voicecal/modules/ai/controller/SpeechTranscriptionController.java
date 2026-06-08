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
 * 璇煶杞啓鍏煎鍏ュ彛锛岄潰鍚戝墠绔闊抽摼璺€?
 */
@RestController
@RequestMapping("/api/voice")
public class SpeechTranscriptionController {

    private final SpeechTranscriptionService speechTranscriptionService;

    public SpeechTranscriptionController(SpeechTranscriptionService speechTranscriptionService) {
        this.speechTranscriptionService = speechTranscriptionService;
    }

    /**
     * 涓婁紶闊抽骞惰繑鍥炶瘑鍒枃鏈€?
     *
     * @param audio 闊抽鏂囦欢
     * @param language 鍙€夎瑷€鍙傛暟锛屽綋鍓嶉鐣?
     * @param contextPrompt 鍙€変笂涓嬫枃鎻愮ず锛屽綋鍓嶉鐣?
     * @return 璇嗗埆缁撴灉
     */
    @PostMapping("/transcribe")
    public ApiResponse<SpeechTranscriptionResponse> transcribe(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "contextPrompt", required = false) String contextPrompt
    ) {
        return ApiResponse.success("璇嗗埆鎴愬姛", speechTranscriptionService.transcribe(audio));
    }
}
