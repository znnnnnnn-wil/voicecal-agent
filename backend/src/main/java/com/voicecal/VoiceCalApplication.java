package com.voicecal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * VoiceCal Agent 后端服务启动入口。
 */
@SpringBootApplication
@EnableScheduling
public class VoiceCalApplication {

    /**
     * 启动 Spring Boot 应用。
     *
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(VoiceCalApplication.class, args);
    }
}
