package com.voicecal.modules.log.service;

import com.voicecal.modules.log.response.VoiceCommandLogResponse;
import java.util.List;

/**
 * 语音命令操作日志服务接口。
 */
public interface VoiceCommandLogService {

    /**
     * 保存语音命令操作日志。
     *
     * @param conversationId 对话 ID
     * @param rawText 用户原始输入
     * @param assistantReply AI 回复
     * @param intent 意图
     * @param toolName 工具名称
     * @param toolArgsJson 工具参数 JSON
     * @param toolResultJson 工具结果 JSON
     * @param success 是否成功
     * @return 日志响应对象
     */
    VoiceCommandLogResponse saveLog(
            String conversationId,
            String rawText,
            String assistantReply,
            String intent,
            String toolName,
            String toolArgsJson,
            String toolResultJson,
            boolean success
    );

    /**
     * 查询最近日志。
     *
     * @param limit 数量限制
     * @return 最近日志列表
     */
    List<VoiceCommandLogResponse> getRecentLogs(Integer limit);

    /**
     * 查询指定对话的最近日志。
     *
     * @param conversationId 对话 ID
     * @param limit 数量限制
     * @return 指定对话的最近日志列表
     */
    List<VoiceCommandLogResponse> getRecentLogs(String conversationId, Integer limit);
}
