package com.voicecal.modules.calendar.service;

import com.voicecal.common.enums.dao.EventCategory;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * 日程分类推断器，基于标题关键词做轻量分类。
 */
@Component
public class CalendarEventCategoryResolver {

    /**
     * 根据显式分类或标题推断日程分类。
     *
     * @param explicitCategory 请求中显式传入的分类，可为空
     * @param title 日程标题
     * @return 日程分类
     */
    public EventCategory resolve(EventCategory explicitCategory, String title) {
        if (explicitCategory != null) {
            return explicitCategory;
        }
        return infer(title);
    }

    /**
     * 根据标题关键词推断分类。
     *
     * @param title 日程标题
     * @return 推断出的分类，无法匹配时返回 OTHER
     */
    public EventCategory infer(String title) {
        if (title == null || title.isBlank()) {
            return EventCategory.OTHER;
        }

        String normalizedTitle = title.toLowerCase(Locale.ROOT);
        if (containsAny(normalizedTitle, "面试", "interview")) {
            return EventCategory.INTERVIEW;
        }
        if (containsAny(normalizedTitle, "会议", "开会", "评审", "周会", "例会", "晨会", "早会", "meeting", "review", "sync", "standup")) {
            return EventCategory.MEETING;
        }
        if (containsAny(normalizedTitle, "作业", "学习", "课程", "复习", "study", "homework", "class", "course")) {
            return EventCategory.STUDY;
        }
        if (containsAny(normalizedTitle, "项目", "工作", "代码", "开发", "project", "work", "code", "develop")) {
            return EventCategory.WORK;
        }
        if (containsAny(normalizedTitle, "健身", "吃饭", "购物", "休息", "生活", "gym", "dinner", "lunch", "shopping", "rest")) {
            return EventCategory.LIFE;
        }
        return EventCategory.OTHER;
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
