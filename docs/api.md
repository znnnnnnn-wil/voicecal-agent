# VoiceCal Agent API 文档

## 通用响应

除 ICS 文件下载接口外，后端统一返回 `ApiResponse`：

```json
{
  "success": true,
  "code": "OK",
  "message": "请求成功",
  "data": {},
  "timestamp": "2026-05-30T10:00:00+08:00"
}
```

常见错误：

- `400`：参数缺失、格式错误、枚举值非法、时间范围非法。
- `404`：日程不存在。
- `500`：服务端异常。

时间字段使用 ISO-8601 `LocalDateTime` 格式，例如 `2026-06-01T10:00:00`。

## 1. 健康检查

### GET `/api/health`

用途：检查后端服务是否运行。

## 2. 日程 CRUD

### POST `/api/calendar/events`

用途：创建日程。

请求体：

```json
{
  "title": "产品评审会议",
  "description": "评审本周版本计划",
  "startTime": "2026-06-01T10:00:00",
  "endTime": "2026-06-01T11:00:00",
  "location": "Zoom",
  "reminderMinutes": 15,
  "category": "MEETING"
}
```

字段规则：

- `title` 必填，最大 100 字符。
- `description` 最大 1000 字符。
- `startTime`、`endTime` 必填。
- `endTime` 必须晚于 `startTime`。
- `location` 最大 255 字符。
- `reminderMinutes` 可为空，不能小于 0。
- `category` 可为空，支持 `WORK`、`STUDY`、`LIFE`、`MEETING`、`INTERVIEW`、`OTHER`。

### GET `/api/calendar/events`

用途：按开始时间升序查询日程列表。

Query 参数：

- `category`：可选，按分类筛选，例如 `MEETING`。

响应 `data` 示例：

```json
[
  {
    "id": 1,
    "title": "产品评审会议",
    "description": "评审本周版本计划",
    "startTime": "2026-06-01T10:00:00",
    "endTime": "2026-06-01T11:00:00",
    "location": "Zoom",
    "category": "MEETING",
    "reminderMinutes": 15,
    "reminderTriggered": false,
    "remindedAt": null,
    "createdAt": "2026-05-30T10:00:00",
    "updatedAt": "2026-05-30T10:00:00"
  }
]
```

### GET `/api/calendar/events/{id}`

用途：查询单个日程详情。

错误情况：`404` 日程不存在。

### PUT `/api/calendar/events/{id}`

用途：更新日程。

请求体与创建日程一致。`category` 不传时保留已有分类。

### DELETE `/api/calendar/events/{id}`

用途：删除日程。

说明：普通 REST CRUD 可直接删除；AI 删除指令也会通过 Calendar Tools 直接执行。若目标不明确，应先由 AI 澄清目标后再执行。

## 3. 日期维度查询

### GET `/api/calendar/events/today`

用途：查询指定时区下今天的日程。

Query 参数：

- `timezone`：可选，默认使用后端服务约定值，例如 `Asia/Shanghai`。

### GET `/api/calendar/events/week`

用途：查询指定时区下本周日程。本周按周一 00:00 到下周一 00:00 计算。

Query 参数：

- `timezone`：可选。

### GET `/api/calendar/events/by-date`

用途：查询指定日期内的日程。

Query 参数：

- `date`：必填，格式 `yyyy-MM-dd`。
- `timezone`：可选。

错误情况：`400` 日期格式或时区非法。

## 4. AI 对话

### POST `/api/ai/chat`

用途：发送用户自然语言指令，返回 AI 回复。

请求体：

```json
{
  "message": "我明天有什么安排？",
  "conversationId": "demo"
}
```

字段规则：

- `message` 必填，最大 2000 字符。
- `conversationId` 可选，最大 100 字符；为空时日志模块使用默认会话。

说明：当前只有存在 `ChatModel` Bean 时才会创建真实 LangChain4j Assistant。无模型配置时返回 fallback 文案。

## 5. 每日摘要

### GET `/api/ai/daily-summary`

用途：查询指定日期的确定性日程摘要。

Query 参数：

- `date`：可选，格式 `yyyy-MM-dd`。
- `timezone`：可选。

响应 `data` 包含：

- `date`
- `timezone`
- `eventCount`
- `busyMinutes`
- `categoryStats`
- `earliestEvent`
- `latestEvent`
- `summary`
- `events`

## 6. 冲突检测

### POST `/api/calendar/events/conflicts`

用途：检测指定时间段是否与已有日程冲突。

请求体：

```json
{
  "startTime": "2026-06-01T10:30:00",
  "endTime": "2026-06-01T11:30:00",
  "excludeEventId": null
}
```

响应 `data` 示例：

```json
{
  "hasConflict": true,
  "conflicts": [
    {
      "id": 1,
      "title": "产品评审会议",
      "startTime": "2026-06-01T10:00:00",
      "endTime": "2026-06-01T11:00:00",
      "location": "Zoom"
    }
  ]
}
```

冲突规则：

```text
existing.startTime < requested.endTime
AND existing.endTime > requested.startTime
```

首尾相接不算冲突。

## 7. 空闲时间

### GET `/api/calendar/events/free-time`

用途：查询指定时间范围内的空闲时间段。

Query 参数：

- `startTime`：必填，ISO-8601 `LocalDateTime`。
- `endTime`：必填，必须晚于 `startTime`。
- `minMinutes`：可选，默认 30，必须大于 0。

示例：

```text
GET /api/calendar/events/free-time?startTime=2026-06-01T13:00:00&endTime=2026-06-01T18:00:00&minMinutes=30
```

## 8. AI 日程操作

AI 日程创建、修改、删除、冲突检测、空闲时间查询和 ICS 导出通过 LangChain4j Tool Calling 调用后端 Calendar Tools。

当前实现不再提供 PendingAction 确认接口。修改或删除指令会在目标明确时直接执行；如果用户表达不明确，例如同名日程有多个候选，AI 应先澄清目标，避免误删或误改。

## 9. 操作日志

### GET `/api/logs/recent`

用途：查询最近 AI 对话操作日志。

Query 参数：

- `limit`：可选，默认 20；Service 会限制最大返回数量。

## 10. 提醒

### GET `/api/reminders/recent`

用途：查询最近已触发提醒。

Query 参数：

- `limit`：可选，默认 20。

说明：提醒机制只记录状态，不做短信、邮件、浏览器通知或 WebSocket 推送。

## 11. ICS 导出

### GET `/api/calendar/events/{id}/ics`

用途：导出单个日程 `.ics` 文件。

成功响应：

- `Content-Type: text/calendar; charset=UTF-8`
- `Content-Disposition: attachment; filename="voicecal-event-{id}.ics"`
- Body 为纯文本 ICS 内容，不包 `ApiResponse`。

错误情况：

- `404`：日程不存在。

### GET `/api/calendar/events/ics`

用途：按时间范围导出多个日程 `.ics` 文件。

Query 参数：

- `startTime`：必填，ISO-8601 `LocalDateTime`。
- `endTime`：必填，必须晚于 `startTime`。

成功响应：

- `Content-Type: text/calendar; charset=UTF-8`
- `Content-Disposition: attachment; filename="voicecal-events.ics"`
- Body 为包含多个 `VEVENT` 的 `VCALENDAR`。

说明：ICS 导出是 `.ics` 文件下载能力，不代表已接入 Google Calendar 同步。
