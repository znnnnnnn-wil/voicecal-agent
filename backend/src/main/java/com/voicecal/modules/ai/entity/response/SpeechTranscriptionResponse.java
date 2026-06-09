package com.voicecal.modules.ai.entity.response;

/**
 * 语音转文本响应对象。
 *
 * @param text 识别出的文本
 * @param model ASR 模型
 * @param durationMs 识别耗时
 * @param success 是否成功
 * @param message 结果消息
 */
public record SpeechTranscriptionResponse(
        String text,
        String model,
        long durationMs,
        boolean success,
        String message
) {

    public SpeechTranscriptionResponse(String text) {
        this(text, null, 0, true, "识别成功");
    }
}
