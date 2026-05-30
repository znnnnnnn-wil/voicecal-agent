package com.voicecal.modules.ai.controller;

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
@ActiveProfiles("h2")
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
}
