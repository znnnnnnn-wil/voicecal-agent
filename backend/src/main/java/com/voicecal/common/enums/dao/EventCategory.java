package com.voicecal.common.enums.dao;

/**
 * 日程分类枚举，用于标记日程所属的业务场景。
 */
public enum EventCategory {

    /**
     * 工作相关日程。
     */
    WORK("工作"),

    /**
     * 学习相关日程。
     */
    STUDY("学习"),

    /**
     * 生活相关日程。
     */
    LIFE("生活"),

    /**
     * 会议类日程。
     */
    MEETING("会议"),

    /**
     * 面试类日程。
     */
    INTERVIEW("面试"),

    /**
     * 其他未分类日程，作为默认分类。
     */
    OTHER("其他");

    private final String description;

    EventCategory(String description) {
        this.description = description;
    }

    /**
     * 获取分类中文说明。
     *
     * @return 分类中文说明
     */
    public String getDescription() {
        return description;
    }
}
