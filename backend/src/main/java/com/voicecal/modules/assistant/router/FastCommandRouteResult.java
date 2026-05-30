package com.voicecal.modules.assistant.router;

import java.util.Optional;

/**
 * 快速规则路由结果。
 *
 * @param matched 是否命中
 * @param type 路由类型
 */
public record FastCommandRouteResult(boolean matched, FastCommandType type) {

    public static FastCommandRouteResult matched(FastCommandType type) {
        return new FastCommandRouteResult(true, type);
    }

    public static FastCommandRouteResult notMatched() {
        return new FastCommandRouteResult(false, null);
    }

    public Optional<FastCommandType> optionalType() {
        return Optional.ofNullable(type);
    }
}
