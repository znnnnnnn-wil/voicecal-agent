package com.voicecal.modules.log.service.impl;

import com.voicecal.modules.log.entity.VoiceCommandLog;
import com.voicecal.modules.log.repository.VoiceCommandLogRepository;
import com.voicecal.modules.log.response.VoiceCommandLogResponse;
import com.voicecal.modules.log.service.VoiceCommandLogService;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 语音命令操作日志服务实现。
 */
@Service
public class VoiceCommandLogServiceImpl implements VoiceCommandLogService {

    private static final String DEFAULT_CONVERSATION_ID = "default";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 100;
    private static final int RAW_TEXT_MAX_LENGTH = 4000;
    private static final int ASSISTANT_REPLY_MAX_LENGTH = 8000;

    private final VoiceCommandLogRepository voiceCommandLogRepository;

    public VoiceCommandLogServiceImpl(VoiceCommandLogRepository voiceCommandLogRepository) {
        this.voiceCommandLogRepository = voiceCommandLogRepository;
    }

    @Override
    @Transactional
    public VoiceCommandLogResponse saveLog(
            String conversationId,
            String rawText,
            String assistantReply,
            String intent,
            String toolName,
            String toolArgsJson,
            String toolResultJson,
            boolean success
    ) {
        VoiceCommandLog log = new VoiceCommandLog();
        log.setConversationId(resolveConversationId(conversationId));
        log.setRawText(truncate(rawText, RAW_TEXT_MAX_LENGTH));
        log.setAssistantReply(truncate(assistantReply, ASSISTANT_REPLY_MAX_LENGTH));
        log.setIntent(intent);
        log.setToolName(toolName);
        log.setToolArgsJson(toolArgsJson);
        log.setToolResultJson(toolResultJson);
        log.setSuccess(success);
        return VoiceCommandLogResponse.from(voiceCommandLogRepository.save(log));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoiceCommandLogResponse> getRecentLogs(Integer limit) {
        int resolvedLimit = resolveLimit(limit);
        return voiceCommandLogRepository.findAllByOrderByCreatedAtDescIdDesc(PageRequest.of(0, resolvedLimit))
                .stream()
                .map(VoiceCommandLogResponse::from)
                .toList();
    }

    private String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return DEFAULT_CONVERSATION_ID;
        }
        return conversationId;
    }

    private int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.min(Math.max(limit, MIN_LIMIT), MAX_LIMIT);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
