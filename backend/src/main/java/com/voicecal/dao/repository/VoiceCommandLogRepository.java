package com.voicecal.dao.repository;

import com.voicecal.dao.entity.VoiceCommandLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 语音命令操作日志数据访问接口。
 */
public interface VoiceCommandLogRepository extends JpaRepository<VoiceCommandLog, Long> {

    /**
     * 按创建时间倒序查询日志。
     *
     * @param pageable 分页参数
     * @return 日志分页结果
     */
    Page<VoiceCommandLog> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);

    /**
     * 按对话 ID 查询最近日志。
     *
     * @param conversationId 对话 ID
     * @param pageable 分页参数
     * @return 指定对话的日志分页结果
     */
    Page<VoiceCommandLog> findByConversationIdOrderByCreatedAtDescIdDesc(String conversationId, Pageable pageable);
}
