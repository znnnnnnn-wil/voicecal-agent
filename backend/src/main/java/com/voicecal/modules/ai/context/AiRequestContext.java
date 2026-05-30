package com.voicecal.modules.ai.context;

/**
 * AI 请求上下文，用于工具调用阶段读取当前用户原始输入。
 */
public final class AiRequestContext {

    private static final ThreadLocal<String> USER_MESSAGE = new ThreadLocal<>();

    private AiRequestContext() {
    }

    public static void setUserMessage(String userMessage) {
        USER_MESSAGE.set(userMessage);
    }

    public static String getUserMessage() {
        return USER_MESSAGE.get();
    }

    public static void clear() {
        USER_MESSAGE.remove();
    }
}
