package com.voicecal.modules.ai.response;

/**
 * 语音转文本响应对象。
 *
 * @param text 识别出的文本
 */
public record SpeechTranscriptionResponse(String text) {
}
