package com.voicecal.modules.health.controller;

import com.voicecal.modules.health.entity.response.HealthResponse;
import com.voicecal.modules.health.service.HealthService;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 健康检查接口测试。
 */
@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthService healthService;

    /**
     * 验证健康检查接口保持可访问，并返回服务层提供的健康状态。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void healthShouldReturnUpStatus() throws Exception {
        when(healthService.getHealthStatus())
                .thenReturn(new HealthResponse("UP", OffsetDateTime.parse("2026-05-29T11:00:00+08:00"), "h2"));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.profile").value("h2"));
    }
}
