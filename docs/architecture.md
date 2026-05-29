# VoiceCal Agent 架构文档

## 1. 系统整体架构

VoiceCal Agent 采用前后端分离的本地 Demo 架构：

```text
React Dashboard
  ↓ HTTP / JSON
Spring Boot API
  ↓
Calendar Service / AI Service / Log Service / Reminder Scheduler
  ↓
Spring Data JPA
  ↓
H2 / MySQL
```

前端负责语音输入、文本输入、日历可视化、摘要展示、日志展示和 Demo 交互反馈。后端负责日程业务、AI Tool Calling 编排、操作日志、提醒扫描、ICS 导出和统一 API 响应。

## 2. 前后端交互流程

典型 AI 指令流程：

1. 用户在前端语音助手中输入语音或文本。
2. 前端调用 `POST /api/ai/chat`。
3. 后端 AI Chat Service 调用 LangChain4j Assistant；没有真实模型时返回 fallback 文案。
4. 如果 Assistant 调用 Tool，Tool 会继续调用 Calendar Service 或 PendingAction Service。
5. 后端返回 AI 回复，并记录 VoiceCommandLog。
6. 前端展示回复，触发语音播报，并刷新日历、摘要、提醒和操作日志区域。

## 3. LangChain4j Tool Calling 流程

```text
自然语言输入
  ↓
VoiceCalAssistant
  ↓
CalendarEventTools
  ↓
CalendarEventService / CalendarAvailabilityService / PendingActionService
  ↓
CalendarEventRepository / InMemoryPendingActionStore
```

当前 `VoiceCalAssistant` 仅在 Spring 容器中存在 `ChatModel` Bean 时创建。未配置模型时，AI Chat 接口不会调用真实外部模型，而是返回本地 fallback 文案，保证 Demo 和测试环境不依赖 API Key。

## 4. 日程操作流程

日程主模块基于 `CalendarEvent` 实体和 `CalendarEventService`：

- 创建：校验标题、描述、开始/结束时间、地点、提醒分钟数和分类。
- 查询：按开始时间升序返回日程，可按 `category` 轻量筛选。
- 修改：复用更新请求校验，并保留或更新分类、提醒字段。
- 删除：普通 REST CRUD 可直接删除；AI Tool 中的删除走 PendingAction 确认。
- 分类：支持 `WORK`、`STUDY`、`LIFE`、`MEETING`、`INTERVIEW`、`OTHER`。
- 提醒字段：事件响应包含 `reminderMinutes`、`reminderTriggered`、`remindedAt`。
- ICS 导出：`IcsExportController` 调用 `IcsExportService` 生成 `text/calendar` 文件内容。

## 5. 冲突检测与空闲时间流程

冲突检测和空闲时间查询统一使用 overlap 规则：

```text
existing.startTime < requested.endTime
AND
existing.endTime > requested.startTime
```

该规则覆盖完全重叠、部分重叠、新事件包含已有事件、已有事件包含新事件。首尾相接不算冲突，例如 `10:00-11:00` 与 `11:00-12:00` 不重叠。

空闲时间计算流程：

1. 查询与目标时间范围重叠的事件。
2. 按 `startTime` 升序排列。
3. 从查询开始时间建立游标。
4. 遍历忙碌事件并合并重叠或首尾相接的占用区间。
5. 输出长度大于等于 `minMinutes` 的空闲区间。

## 6. 危险操作确认流程

AI Tool 不直接执行删除或高影响修改，而是使用 PendingAction：

1. 用户提出删除或修改意图。
2. Tool 创建 `DELETE_EVENT` 或 `UPDATE_EVENT` 类型的 PendingAction。
3. PendingAction 保存操作 ID、conversationId、payloadJson、目标摘要和过期时间。
4. 用户明确确认后，调用 confirm 执行真实删除或修改。
5. 用户取消或操作过期时，不执行任何日程变更。

REST API 也提供待确认操作的查询、创建、确认和取消能力，便于后续前端扩展。

## 7. 提醒扫描流程

提醒机制是 MVP 状态记录，不做真实推送：

1. `ReminderScheduler` 定时调用 `ReminderService`。
2. Service 查询未触发且设置了 `reminderMinutes` 的候选日程。
3. 当 `now >= startTime - reminderMinutes` 且 `now < startTime` 时触发提醒。
4. 触发后设置 `reminderTriggered = true`，并写入 `remindedAt`。
5. 前端通过事件响应和 `GET /api/reminders/recent` 展示提醒状态。

当前不实现短信、邮件、浏览器通知、WebSocket 或消息队列。

## 8. ICS 导出流程

ICS 导出支持两种方式：

- 单个事件导出：`GET /api/calendar/events/{id}/ics`
- 时间范围导出：`GET /api/calendar/events/ics?startTime=...&endTime=...`

后端生成基础 iCalendar 内容：

- `BEGIN:VCALENDAR` / `END:VCALENDAR`
- `VERSION:2.0`
- `PRODID:-//VoiceCal Agent//CN`
- `VEVENT`
- `UID`
- `DTSTAMP`
- `DTSTART`
- `DTEND`
- `SUMMARY`
- 可选 `DESCRIPTION` 和 `LOCATION`

时间范围导出复用 overlap 查询规则。文本字段会转义反斜杠、换行、分号和逗号。ICS 导出是文件格式导出，不等于 Google Calendar 同步。

## 9. 操作日志流程

操作日志模块记录 AI 对话的可解释性信息：

- conversationId
- 用户原始输入 rawText
- AI 回复 assistantReply
- intent
- toolName / toolArgsJson / toolResultJson，可为空
- success
- createdAt

`AiChatServiceImpl` 在 AI 请求成功或失败时尝试写入日志。日志写入失败不会影响 AI Chat 主流程。前端通过 `GET /api/logs/recent` 展示最近日志。

## 10. Google Calendar 扩展设计

Google Calendar 同步是未来扩展，不是当前已实现能力。完整设计见 [google-calendar-sync-design.md](google-calendar-sync-design.md)。

该设计包含 CalendarProvider 抽象、Google OAuth2 授权流程、本地事件与外部事件映射、单向和双向同步策略、冲突处理、增量同步、token 安全存储、外部 API 失败重试与限流。
