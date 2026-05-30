package com.voicecal.modules.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voicecal.common.enums.ResultCodeEnum;
import com.voicecal.common.exception.CustomException;
import com.voicecal.modules.ai.response.SpeechTranscriptionResponse;
import com.voicecal.modules.ai.service.SpeechTranscriptionService;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 基于 Qwen3-ASR-Flash 的语音转文本服务实现。
 */
@Service
public class SpeechTranscriptionServiceImpl implements SpeechTranscriptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpeechTranscriptionServiceImpl.class);
    private static final String DEFAULT_AUDIO_MIME_TYPE = "audio/webm";
    private static final int MAX_LOG_BODY_LENGTH = 1000;
    private static final String CONTEXT_PROMPT = """
            VoiceCal Agent 是语音日历助手。常见词包括：日程、会议、提醒、提交代码、项目、面试、今天、明天、后天、上午、下午、晚上、下周、空闲、删除、确认、取消、导出 ICS。常见分类包括：工作、学习、生活、会议、面试、其他。
            """;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String apiKey;
    private final String modelName;
    private final long timeoutSeconds;
    private final long maxAudioBytes;

    public SpeechTranscriptionServiceImpl(
            ObjectMapper objectMapper,
            @Value("${voicecal.ai.speech.base-url}") String baseUrl,
            @Value("${voicecal.ai.speech.api-key}") String apiKey,
            @Value("${voicecal.ai.speech.model-name}") String modelName,
            @Value("${voicecal.ai.speech.timeout-seconds}") Long timeoutSeconds,
            @Value("${voicecal.ai.speech.max-audio-bytes}") Long maxAudioBytes
    ) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.timeoutSeconds = timeoutSeconds;
        this.maxAudioBytes = maxAudioBytes;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    /**
     * 识别前端上传的短音频。
     *
     * @param audio 音频文件
     * @return 语音识别结果
     */
    @Override
    public SpeechTranscriptionResponse transcribe(MultipartFile audio) {
        long startedAt = System.currentTimeMillis();
        validateAudio(audio);
        if (apiKey == null || apiKey.isBlank()) {
            throw CustomException.create(ResultCodeEnum.PARAMS_ERROR, "语音识别服务未配置 DASHSCOPE_API_KEY");
        }

        try {
            String requestBody = buildRequestBody(audio);
            LOGGER.info(
                    "开始语音识别，model={}, contentType={}, size={} bytes",
                    modelName,
                    audio.getContentType(),
                    audio.getSize()
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl(baseUrl) + "/chat/completions"))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseResponse(response, System.currentTimeMillis() - startedAt);
        } catch (IOException exception) {
            LOGGER.warn("语音识别读取或网络请求失败", exception);
            throw CustomException.create(ResultCodeEnum.FAIL, "读取或识别音频失败");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOGGER.warn("语音识别请求被中断", exception);
            throw CustomException.create(ResultCodeEnum.FAIL, "语音识别请求被中断");
        }
    }

    private void validateAudio(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) {
            throw CustomException.create(ResultCodeEnum.PARAMS_ERROR, "音频文件不能为空");
        }
        if (audio.getSize() > maxAudioBytes) {
            throw CustomException.create(ResultCodeEnum.PARAMS_ERROR, "音频文件不能超过 10 MB");
        }
    }

    private String buildRequestBody(MultipartFile audio) throws IOException {
        String mimeType = audio.getContentType();
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = DEFAULT_AUDIO_MIME_TYPE;
        }
        String dataUrl = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(audio.getBytes());
        Map<String, Object> request = Map.of(
                "model", modelName,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", List.of(Map.of("type", "text", "text", CONTEXT_PROMPT))
                        ),
                        Map.of(
                                "role", "user",
                                "content", List.of(Map.of(
                                        "type", "input_audio",
                                        "input_audio", Map.of("data", dataUrl)
                                ))
                        )
                ),
                "asr_options", Map.of("enable_itn", true),
                "stream", false
        );
        return objectMapper.writeValueAsString(request);
    }

    private SpeechTranscriptionResponse parseResponse(HttpResponse<String> response, long durationMs) throws IOException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            LOGGER.warn(
                    "语音识别服务返回异常，status={}, body={}",
                    response.statusCode(),
                    truncateForLog(response.body())
            );
            throw CustomException.create(ResultCodeEnum.FAIL, "语音识别服务返回异常，请查看后端日志中的 ASR 响应详情");
        }
        JsonNode root = objectMapper.readTree(response.body());
        String text = root.path("choices").path(0).path("message").path("content").asText("").trim();
        if (text.isBlank()) {
            LOGGER.warn("语音识别结果为空，body={}", truncateForLog(response.body()));
            throw CustomException.create(ResultCodeEnum.FAIL, "语音识别结果为空");
        }
        LOGGER.info("语音识别成功，model={}, durationMs={}, textLength={}", modelName, durationMs, text.length());
        return new SpeechTranscriptionResponse(text, modelName, durationMs, true, "识别成功");
    }

    private String normalizeBaseUrl(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String truncateForLog(String body) {
        if (body == null || body.length() <= MAX_LOG_BODY_LENGTH) {
            return body;
        }
        return body.substring(0, MAX_LOG_BODY_LENGTH) + "...";
    }
}
