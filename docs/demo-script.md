# VoiceCal Agent Demo 脚本

本文档用于比赛路演、评审演示或 Demo 视频录制。脚本以本地运行环境为基础，不依赖真实 Google Calendar、OAuth 或外部推送服务。

## 1. 开场介绍

VoiceCal Agent 是一个语音驱动的 AI 日历助手。用户可以通过语音或文本输入自然语言指令，完成日程创建、查询、冲突检测、空闲时间查询、危险操作确认、每日摘要和日历可视化展示。

本项目是本地 Demo：后端使用 Spring Boot，前端使用 React Dashboard。Google Calendar 同步目前是扩展设计，不是真实接入能力。

## 2. 启动项目

后端：

```powershell
mvn.cmd -f backend\pom.xml spring-boot:run
```

前端：

```powershell
cd frontend
npm install
npm run dev
```

如果使用默认 Vite 配置，打开终端输出中的本地地址，例如：

```text
http://localhost:5173/
```

## 3. 打开首页

介绍页面主要区域：

- 语音助手：支持麦克风输入、文本兜底输入、示例指令、AI 回复和语音播报。
- 日历视图：展示月视图和周视图中的真实日程。
- 事件详情：点击日程后展示标题、时间、地点、分类、提醒状态和 ICS 导出入口。
- 今日安排与本周摘要：辅助展示近期日程。
- 每日摘要：展示日程总数、忙碌时长、分类统计、重点安排和摘要文案。
- 操作日志：展示最近 AI 指令、回复、状态和时间。

如果后端未启动，前端会显示明确的 demo fallback 文案，例如 “Showing demo data because the backend is unavailable.”。

## 4. 语音或文本创建日程

示例指令：

```text
明天下午三点提醒我提交项目代码
```

演示步骤：

1. 点击麦克风按钮说出指令，或直接在文本框输入。
2. 识别完成后，确认文本内容。
3. 点击发送。

预期效果：

- AI 回复区域展示处理结果。
- 如果配置了真实 ChatModel，Assistant 可以通过 Tool Calling 创建日程。
- 如果未配置真实模型，接口会返回 fallback 文案，前端仍能演示语音输入、文本兜底、语音播报和错误/降级状态。
- 日历、今日安排、本周摘要和操作日志会在对话完成后刷新。

## 5. 查询日程

示例指令：

```text
我明天有什么安排？
```

预期效果：

- AI 回复明日安排或 fallback 文案。
- 日历和摘要区域提供可视化辅助。
- 操作日志出现一条新的 AI 对话记录。

## 6. 冲突检测

示例指令：

```text
明天下午三点再安排一个项目评审
```

说明：

- 后端提供冲突检测能力，重叠规则为 `existing.startTime < requested.endTime AND existing.endTime > requested.startTime`。
- 如果真实 Assistant 调用冲突检测 Tool，可以返回冲突事件。
- 当前前端不做单独冲突弹窗，冲突结果主要通过 AI 回复或 API 验证体现。

## 7. 空闲时间查询

示例指令：

```text
我周五下午有空吗？
```

或：

```text
帮我找一下周五下午一小时的空闲时间
```

预期效果：

- 后端按目标时间范围计算空闲时间段。
- AI 回复可用时间段或说明没有满足条件的空闲时间。

## 8. 危险操作确认

示例指令：

```text
删除明天下午的会议
```

预期流程：

1. AI 不直接删除日程。
2. 系统创建一个待确认删除操作 PendingAction。
3. 用户输入：

```text
确认
```

4. 系统才执行真实删除。

取消流程：

```text
取消
```

取消后 PendingAction 被移除，日程不会被删除或修改。

说明：如果有多个候选事件，理想流程是先让用户明确选择目标，不自动执行危险操作。

## 9. 每日摘要

示例指令：

```text
给我今天的日程摘要
```

页面预期展示：

- 今日日程总数
- 忙碌分钟数或小时数
- 最早一项日程
- 最晚一项日程
- 分类统计
- 确定性摘要文案

每日摘要不依赖真实外部 LLM，因此在本地环境可稳定验证。

## 10. 提醒状态

说明：

- 日程可设置 `reminderMinutes`，表示开始前多少分钟提醒。
- 后端定时扫描即将到达提醒时间的日程。
- 触发后写入 `reminderTriggered = true` 和 `remindedAt`。
- 本项目 MVP 不做短信、邮件、浏览器 Notification、WebSocket 或真实推送。

演示方式：

1. 创建带 `reminderMinutes` 的日程。
2. 等待后端扫描或使用测试数据。
3. 在事件详情、今日安排或最近提醒区域查看提醒状态。

## 11. ICS 导出

演示方式：

1. 在日历视图中点击一个事件。
2. 在事件详情面板点击 “导出 ICS”。
3. 前端会请求 `/api/calendar/events/{id}/ics` 并下载 `.ics` 文件。

注意：

- 当前前端已有 ICS 下载入口。
- 请先确认当前后端分支是否已提供对应 ICS Controller。
- ICS 文件可用于导入外部日历应用，但这不等于 Google Calendar 真实同步。

## 12. 操作日志

演示方式：

1. 发送任意 AI 指令。
2. 查看 Operation Log 区域。

预期展示：

- 用户输入 rawText
- AI 回复 assistantReply
- conversationId
- 成功或失败状态
- 创建时间

日志模块帮助评委理解系统如何记录 AI 日历助手的交互过程。

## 13. Google Calendar 扩展设计

说明：

- 当前项目没有真实接入 Google Calendar。
- 当前项目没有 OAuth 页面，也不保存 Google token。
- 已提供 [Google Calendar 同步扩展设计](google-calendar-sync-design.md)，说明未来如何通过 OAuth2、CalendarProvider 抽象、映射表、增量同步和冲突处理扩展外部日历同步能力。

演示时可以强调：当前已经通过 ICS 导出和 Provider 设计为未来外部同步预留方向，但不会夸大为已实现 Google Calendar 同步。

## 14. 收尾总结

VoiceCal Agent 的 Demo 亮点：

- 语音输入与文本兜底
- AI Tool Calling 日历操作
- 本地日程 CRUD
- 冲突检测和空闲时间查询
- 危险操作确认机制
- 日历可视化 Dashboard
- 每日摘要和分类统计
- 操作日志可解释性
- 提醒状态 MVP
- ICS 导出入口
- Google Calendar 同步扩展设计
