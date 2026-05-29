package com.voicecal.common.enums.dao;

/**
 * 日程状态枚举，用于区分正常、取消和软删除状态的日程记录。
 */
public enum EventStatus {

    /**
     * 正常有效日程。
     */
    ACTIVE("正常"),

    /**
     * 已取消日程。
     */
    CANCELLED("已取消"),

    /**
     * 已删除日程，用于软删除。
     */
    DELETED("已删除");

    private final String description;

    EventStatus(String description) {
        this.description = description;
    }

    /**
     * 获取状态中文说明。
     *
     * @return 状态中文说明
     */
    public String getDescription() {
        return description;
    }
}
