package com.voicecal.modules.calendar.service;

import com.voicecal.common.enums.dao.EventCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 日程分类推断测试。
 */
class CalendarEventCategoryResolverTest {

    private final CalendarEventCategoryResolver resolver = new CalendarEventCategoryResolver();

    @Test
    void categoryInference_shouldBeCaseInsensitive() {
        assertThat(resolver.infer("Interview with team")).isEqualTo(EventCategory.INTERVIEW);
    }

    @Test
    void categoryInference_shouldTreatKaiHuiAsMeeting() {
        assertThat(resolver.infer("明天下午开会")).isEqualTo(EventCategory.MEETING);
    }

    @Test
    void categoryInference_shouldReturnOther_whenTitleIsBlank() {
        assertThat(resolver.infer("  ")).isEqualTo(EventCategory.OTHER);
    }
}
