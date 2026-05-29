package com.voicecal.modules.calendar.entity.response;

import java.util.List;

/**
 * 日程冲突检测响应对象。
 *
 * @param hasConflict 是否存在冲突
 * @param conflicts 冲突日程列表
 */
public record ConflictCheckResponse(
        boolean hasConflict,
        List<ConflictEventResponse> conflicts
) {
}
