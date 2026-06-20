package com.voicecal.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 语音命令操作日志实体，记录用户输入、AI 回复和执行结果。
 */
@Entity
@Table(
        name = "voice_command_log",
        indexes = {
                @Index(name = "idx_voice_command_log_created_at", columnList = "created_at"),
                @Index(name = "idx_voice_command_log_conversation_id", columnList = "conversation_id")
        }
)
public class VoiceCommandLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false, length = 100)
    private String conversationId;

    @Column(name = "raw_text", nullable = false, length = 4000)
    private String rawText;

    @Column(name = "assistant_reply", length = 8000)
    private String assistantReply;

    @Column(name = "intent", length = 100)
    private String intent;

    @Column(name = "tool_name", length = 100)
    private String toolName;

    @Lob
    @Column(name = "tool_args_json")
    private String toolArgsJson;

    @Lob
    @Column(name = "tool_result_json")
    private String toolResultJson;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public VoiceCommandLog() {
    }

    /**
     * 持久化前设置创建时间。
     */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = now();
        }
    }

    private static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getAssistantReply() {
        return assistantReply;
    }

    public void setAssistantReply(String assistantReply) {
        this.assistantReply = assistantReply;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getToolArgsJson() {
        return toolArgsJson;
    }

    public void setToolArgsJson(String toolArgsJson) {
        this.toolArgsJson = toolArgsJson;
    }

    public String getToolResultJson() {
        return toolResultJson;
    }

    public void setToolResultJson(String toolResultJson) {
        this.toolResultJson = toolResultJson;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
