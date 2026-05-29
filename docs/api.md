# VoiceCal Agent API 文档

## 通用响应

除文件下载类接口外，后端统一返回 `ApiResponse`：

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
- `404`：日程或待确认操作不存在。
- `410`：待确认操作已过期。
- `500`：服务端异常。

时间字段使用 ISO-8601 `LocalDateTime` 格式，例如 `2026-06-01T10:00:00`。

## 1. 健康检查

### GET `/api/health`

用途：检查后端服务是否运行。

响应示例：

```json
{
  "success": true,
  "code": "OK",
  "message": "请求成功",
  "data": {
    "status": "UP",
    "service": "voicecal-agent",
    "profile": "h2"
  },
  "timestamp": "2026-05-30T10:00:00+08:00"
}
```

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

错误情况：

- `404`：日程不存在。

### PUT `/api/calendar/events/{id}`

用途：更新日程。

请求体与创建日程一致。`category` 不传时保留已有分类。

错误情况：

- `400`：参数非法。
- `404`：日程不存在。

### DELETE `/api/calendar/events/{id}`

用途：删除日程。

说明：普通 REST CRUD 可直接删除；AI Tool 中的删除操作应走 PendingAction 确认流程。

错误情况：

- `404`：日程不存在。

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

错误情况：

- `400`：日期格式或时区非法。

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

响应 `data` 示例：

```json
{
  "reply": "AI provider is not configured yet. VoiceCal calendar tools are registered and ready for use when a chat model is configured."
}
```

说明：当前只有存在 `ChatModel` Bean 时才会创建真实 LangChain4j Assistant。无模型配置时返回 fallback 文案。

## 5. 每日摘要

### GET `/api/ai/daily-summary`

用途：查询指定日期的确定性日程摘要。

Query 参数：

- `date`：可选，格式 `yyyy-MM-dd`。
- `timezone`：可选。

响应 `data` 示例：

```json
{
  "date": "2026-06-01",
  "timezone": "Asia/Shanghai",
  "eventCount": 3,
  "busyMinutes": 180,
  "categoryStats": {
    "MEETING": 2,
    "WORK": 1
  },
  "earliestEvent": {
    "id": 1,
    "title": "晨会",
    "startTime": "2026-06-01T09:00:00",
    "endTime": "2026-06-01T09:30:00",
    "category": "MEETING"
  },
  "latestEvent": {
    "id": 3,
    "title": "代码提交",
    "startTime": "2026-06-01T17:00:00",
    "endTime": "2026-06-01T18:00:00",
    "category": "WORK"
  },
  "summary": "You have 3 events today...",
  "events": []
}
```

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

响应 `data` 示例：

```json
[
  {
    "startTime": "2026-06-01T13:00:00",
    "endTime": "2026-06-01T15:00:00",
    "minutes": 120
  }
]
```

## 8. PendingAction

### POST `/api/pending-actions/delete-event`

用途：创建待确认删除日程操作，不直接删除日程。

请求体：

```json
{
  "conversationId": "demo",
  "eventId": 1
}
```

### POST `/api/pending-actions/update-event`

用途：创建待确认更新日程操作，不直接修改日程。

请求体：

```json
{
  "conversationId": "demo",
  "eventId": 1,
  "updateRequest": {
    "title": "更新后的会议",
    "description": "调整会议时间",
    "startTime": "2026-06-01T14:00:00",
    "endTime": "2026-06-01T15:00:00",
    "location": "Zoom",
    "reminderMinutes": 10,
    "category": "MEETING"
  }
}
```

### GET `/api/pending-actions`

用途：查询指定对话下的待确认操作。

Query 参数：

- `conversationId`：可选，默认 `default`。

### POST `/api/pending-actions/{id}/confirm`

用途：确认并执行待确认操作。

Query 参数：

- `conversationId`：可选，默认 `default`。

错误情况：

- `404`：操作不存在或 conversationId 不匹配。
- `410`：操作已过期。

### POST `/api/pending-actions/{id}/cancel`

用途：取消待确认操作，不执行真实日程变更。

Query 参数：

- `conversationId`：可选，默认 `default`。

## 9. 操作日志

### GET `/api/logs/recent`

用途：查询最近 AI 对话操作日志。

Query 参数：

- `limit`：可选，默认 20；Service 会限制最大返回数量。

响应 `data` 示例：

```json
[
  {
    "id": 10,
    "conversationId": "demo",
    "rawText": "我明天有什么安排？",
    "assistantReply": "你明天有 3 个日程...",
    "intent": "CHAT",
    "toolName": null,
    "toolArgsJson": null,
    "toolResultJson": null,
    "success": true,
    "createdAt": "2026-06-01T10:00:00"
  }
]
```

## 10. 提醒

### GET `/api/reminders/recent`

用途：查询最近已触发提醒。

Query 参数：

- `limit`：可选，默认 20。

响应 `data` 示例：

```json
[
  {
    "eventId": 1,
    "title": "产品评审会议",
    "startTime": "2026-06-01T10:00:00",
    "reminderMinutes": 15,
    "reminderTriggered": true,
    "remindedAt": "2026-06-01T09:45:01"
  }
]
```

说明：提醒机制只记录状态，不做短信、邮件、浏览器通知或 WebSocket 推送。

## 11. ICS 导出

前端事件详情中已有 “导出 ICS” 按钮，调用路径：

```text
GET /api/calendar/events/{id}/ics
```

预期成功响应应为 `text/calendar; charset=UTF-8`，不是 `ApiResponse`。前端会将响应作为 Blob 下载为 `voicecal-event-{id}.ics`。

当前后端源码中未检测到对应 ICS Controller 或 Service。如果当前分支尚未补齐该接口，点击导出会返回错误状态；补齐后建议同时支持：

```text
GET /api/calendar/events/{id}/ics
GET /api/calendar/events/ics?startTime=2026-06-01T00:00:00&endTime=2026-06-08T00:00:00
```

注意：ICS 导出是 `.ics` 文件下载能力，不代表已接入 Google Calendar 同步。
