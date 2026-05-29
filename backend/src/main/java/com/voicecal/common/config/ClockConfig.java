package com.voicecal.common.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 系统时间配置，便于定时任务和测试使用可替换的时钟。
 */
@Configuration
public class ClockConfig {

    /**
     * 提供默认系统时钟。
     *
     * @return 系统默认时区时钟
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
