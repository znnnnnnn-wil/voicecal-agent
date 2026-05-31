package com.voicecal.modules.ai.controller;

import com.voicecal.common.enums.ResultCodeEnum;
import com.voicecal.common.exception.CustomException;
import com.voicecal.modules.ai.response.SpeechTranscriptionResponse;
import com.voicecal.modules.ai.service.SpeechTranscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 语音识别接口集成测试。
 */
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "voicecal.ai.qwen.api-key=",
        "voicecal.ai.speech.api-key="
})
class SpeechTranscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpeechTranscriptionService speechTranscriptionService;

    @Test
    void transcribe_shouldReturnText_whenAudioIsUploaded() throws Exception {
        when(speechTranscriptionService.transcribe(any()))
                .thenReturn(new SpeechTranscriptionResponse("明天下午三点提醒我提交项目代码"));
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "command.webm",
                "audio/webm",
                new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/ai/speech/transcriptions").file(audio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("语音识别成功"))
                .andExpect(jsonPath("$.data.text").value("明天下午三点提醒我提交项目代码"));
    }

    @Test
    void transcribeVoiceEndpoint_shouldReturnText_whenAudioIsUploaded() throws Exception {
        when(speechTranscriptionService.transcribe(any()))
                .thenReturn(new SpeechTranscriptionResponse(
                        "今天有什么安排",
                        "qwen3-asr-flash",
                        1234,
                        true,
                        "识别成功"
                ));
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "command.webm",
                "audio/webm",
                new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/voice/transcribe").file(audio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("识别成功"))
                .andExpect(jsonPath("$.data.text").value("今天有什么安排"))
                .andExpect(jsonPath("$.data.model").value("qwen3-asr-flash"))
                .andExpect(jsonPath("$.data.durationMs").value(1234))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void transcribe_shouldReturnFriendlyError_whenServiceFails() throws Exception {
        when(speechTranscriptionService.transcribe(any()))
                .thenThrow(CustomException.create(ResultCodeEnum.FAIL, "语音识别失败，请重试"));
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "command.webm",
                "audio/webm",
                new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/voice/transcribe").file(audio))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("语音识别失败，请重试"));
    }
}
