package com.voicecal.modules.ai.controller;

import com.voicecal.common.enums.ResultCodeEnum;
import com.voicecal.common.exception.CustomException;
import com.voicecal.modules.ai.entity.response.SpeechTranscriptionResponse;
import com.voicecal.modules.ai.service.SpeechTranscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpeechTranscriptionController.class)
class SpeechTranscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpeechTranscriptionService speechTranscriptionService;

    @Test
    void transcribeVoiceEndpoint_shouldReturnText_whenAudioIsUploaded() throws Exception {
        when(speechTranscriptionService.transcribe(any()))
                .thenReturn(new SpeechTranscriptionResponse(
                        "today schedule",
                        "qwen3-asr-flash",
                        1234,
                        true,
                        "ok"
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
                .andExpect(jsonPath("$.message").value("\u8bc6\u522b\u6210\u529f"))
                .andExpect(jsonPath("$.data.text").value("today schedule"))
                .andExpect(jsonPath("$.data.model").value("qwen3-asr-flash"))
                .andExpect(jsonPath("$.data.durationMs").value(1234))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void transcribeVoiceEndpoint_shouldReturnFriendlyError_whenServiceFails() throws Exception {
        when(speechTranscriptionService.transcribe(any()))
                .thenThrow(CustomException.create(ResultCodeEnum.FAIL, "asr failed"));
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "command.webm",
                "audio/webm",
                new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/voice/transcribe").file(audio))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("asr failed"));
    }
}
