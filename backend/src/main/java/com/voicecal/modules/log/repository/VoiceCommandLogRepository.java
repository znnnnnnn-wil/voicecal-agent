package com.voicecal.modules.log.repository;

import com.voicecal.modules.log.entity.VoiceCommandLog;
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
}
