package com.voicecal.modules.assistant.router;

import org.springframework.stereotype.Component;

/**
 * 高确定性查询类指令的轻量规则路由。
 */
@Component
public class FastCommandRouter {

    /**
     * 尝试识别安全查询类指令。
     *
     * @param message 用户原始消息
     * @return 路由结果
     */
    public FastCommandRouteResult tryRoute(String message) {
        String normalized = normalize(message);
        if (normalized.isBlank() || isRiskyCommand(normalized)) {
            return FastCommandRouteResult.notMatched();
        }
        if (containsAny(normalized, "今天有什么安排", "今天有什么日程", "今天的安排", "今日安排", "我今天有什么安排")) {
            return FastCommandRouteResult.matched(FastCommandType.TODAY_EVENTS);
        }
        if (containsAny(normalized, "明天有什么安排", "明天有什么日程", "明天的安排", "我明天有什么安排")) {
            return FastCommandRouteResult.matched(FastCommandType.TOMORROW_EVENTS);
        }
        if (containsAny(normalized, "本周日程", "这周有什么安排", "我这周有什么安排", "这周有哪些会议")) {
            return FastCommandRouteResult.matched(FastCommandType.WEEK_EVENTS);
        }
        if (isFreeTimeQuery(normalized)) {
            return FastCommandRouteResult.matched(FastCommandType.FREE_TIME);
        }
        return FastCommandRouteResult.notMatched();
    }

    private boolean isFreeTimeQuery(String message) {
        return containsAny(message, "有空吗", "有没有空", "有时间吗", "有没有时间", "空闲吗")
                && containsAny(message, "今天", "明天", "后天", "本周", "这周", "下周", "周", "星期")
                && containsAny(message, "上午", "中午", "下午", "晚上", "今晚", "白天");
    }

    private boolean isRiskyCommand(String message) {
        return containsAny(
                message,
                "提醒我",
                "创建",
                "新增",
                "安排一个",
                "安排一场",
                "安排会议",
                "安排日程",
                "删除",
                "删掉",
                "取消",
                "修改",
                "更新",
                "改到",
                "导出"
        );
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String message) {
        if (message == null) {
            return "";
        }
        return message.toLowerCase()
                .replace("？", "")
                .replace("?", "")
                .replace("，", "")
                .replace(",", "")
                .replace(" ", "")
                .trim();
    }
}
