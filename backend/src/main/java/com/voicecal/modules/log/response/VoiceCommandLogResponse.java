package com.voicecal.modules.log.response;

import com.voicecal.modules.log.entity.VoiceCommandLog;
import java.time.LocalDateTime;

/**
 * 语音命令操作日志响应对象。
 *
 * @param id 日志 ID
 * @param conversationId 对话 ID
 * @param rawText 用户原始输入
 * @param assistantReply AI 回复
 * @param intent 意图
 * @param toolName 工具名称
 * @param toolArgsJson 工具参数 JSON
 * @param toolResultJson 工具结果 JSON
 * @param success 是否成功
 * @param createdAt 创建时间
 */
public record VoiceCommandLogResponse(
        Long id,
        String conversationId,
        String rawText,
        String assistantReply,
        String intent,
        String toolName,
        String toolArgsJson,
        String toolResultJson,
        Boolean success,
        LocalDateTime createdAt
) {

    /**
     * 将实体转换为响应对象。
     *
     * @param log 日志实体
     * @return 响应对象
     */
    public static VoiceCommandLogResponse from(VoiceCommandLog log) {
        return new VoiceCommandLogResponse(
                log.getId(),
                log.getConversationId(),
                log.getRawText(),
                log.getAssistantReply(),
                log.getIntent(),
                log.getToolName(),
                log.getToolArgsJson(),
                log.getToolResultJson(),
                log.getSuccess(),
                log.getCreatedAt()
        );
    }
}
