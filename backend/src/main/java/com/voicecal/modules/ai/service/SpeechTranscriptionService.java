package com.voicecal.modules.ai.service;

import com.voicecal.modules.ai.entity.response.SpeechTranscriptionResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 语音转文本服务接口。
 */
public interface SpeechTranscriptionService {

    /**
     * 识别音频文件并返回文本。
     *
     * @param audio 音频文件
     * @return 语音识别结果
     */
    SpeechTranscriptionResponse transcribe(MultipartFile audio);
}
