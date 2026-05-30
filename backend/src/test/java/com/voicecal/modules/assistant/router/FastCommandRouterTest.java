package com.voicecal.modules.assistant.router;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 快速指令路由测试。
 */
class FastCommandRouterTest {

    private final FastCommandRouter fastCommandRouter = new FastCommandRouter();

    @Test
    void tryRoute_shouldMatchTodayEvents() {
        FastCommandRouteResult result = fastCommandRouter.tryRoute("我今天有什么安排");

        assertThat(result.matched()).isTrue();
        assertThat(result.type()).isEqualTo(FastCommandType.TODAY_EVENTS);
    }

    @Test
    void tryRoute_shouldMatchTomorrowEvents() {
        FastCommandRouteResult result = fastCommandRouter.tryRoute("我明天有什么安排");

        assertThat(result.matched()).isTrue();
        assertThat(result.type()).isEqualTo(FastCommandType.TOMORROW_EVENTS);
    }

    @Test
    void tryRoute_shouldNotHandleCreateCommand() {
        FastCommandRouteResult result = fastCommandRouter.tryRoute("明天下午三点提醒我提交代码");

        assertThat(result.matched()).isFalse();
    }

    @Test
    void tryRoute_shouldNotHandleDeleteCommand() {
        FastCommandRouteResult result = fastCommandRouter.tryRoute("删除明天下午的会议");

        assertThat(result.matched()).isFalse();
    }
}
