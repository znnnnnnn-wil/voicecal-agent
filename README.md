# VoiceCal Agent

> 🎙️ VoiceCal Agent 是一个支持语音和文本指令的 AI 日程助手，用于创建、查询和管理本地日历日程。  
> 项目围绕 **语音识别 + 大模型理解 + 日历工具调用 + 可视化日程管理** 构建，展示 AI Agent 在个人效率工具中的落地方式。

---

## 🎬 Demo 视频

> 本项目核心功能演示视频已上传至 Bilibili，可直接观看。

### 👉 [点击观看 VoiceCal Agent Demo 视频](https://www.bilibili.com/video/BV1qdVU6KEZ8/)

视频覆盖内容：

- 语音创建日程
- 查询今天 / 明天 / 本周安排
- 空闲时间查询
- 冲突检测
- 修改 / 删除确认机制
- 日历可视化展示
- 操作日志、提醒和每日摘要
- README 与项目结构说明

---

## 🌐 在线体验

在线地址：

```text
https://8-130-187-10.sslip.io/
```

> 公网环境下，浏览器语音输入需要 HTTPS 才能正常访问麦克风权限。

---

## 项目简介

VoiceCal Agent 是一个语音驱动的 AI 日历助手，支持用户通过语音、自然语言文本或后端 API 管理本地日程。用户可以说出或输入类似下面的指令：

```text
明天下午三点开项目会
我今天有什么安排？
明天下午有空吗？
把明天下午三点的项目会改到四点
删除明天下午四点的项目会
```

系统会完成语音识别、自然语言理解、日历工具调用和结果展示。对于创建日程，系统会在校验通过后直接创建；对于修改和删除等高风险操作，系统会要求用户确认后再执行，降低误操作风险。

---

## 功能亮点

- 🎙️ **语音驱动日历操作**：前端支持录音、语音状态检测和音频上传，后端完成语音转写并交给 AI 助手处理。
- 🧠 **AI Agent + Tool Calling**：基于 LangChain4j 将日历操作封装为工具，由大模型理解用户意图并调用后端能力。
- ⚡ **FastCommandRouter 快速路由**：对“今天有什么安排”“明天有什么安排”“本周日程”“明天下午有空吗”等高频安全查询直接走后端逻辑，减少大模型等待时间。
- 📅 **FullCalendar 可视化日历**：支持月视图、周视图、日视图和事件详情展示。
- ⚠️ **冲突检测**：创建和更新日程时检查时间段是否与已有日程冲突，避免重复排期。
- 🕒 **空闲时间查询**：根据已有日程计算指定时间范围内的可用时间段。
- 🔐 **修改 / 删除确认保护**：修改和删除属于高风险操作，系统会先要求用户确认，降低误操作风险。
- 🧾 **操作日志可解释**：记录用户输入、AI 回复、路由来源和执行状态，方便回溯 AI Agent 的操作过程。
- 📤 **ICS 导出**：支持单个日程或指定时间范围内的 `.ics` 文件导出。

---

## 技术栈

### 前端

- React `^19.1.1`
- Vite `^7.1.4`
- TypeScript `~5.9.2`
- Tailwind CSS `^4.1.13`
- FullCalendar `^6.1.20`
- MediaRecorder
- Web Audio API
- SpeechSynthesis

### 后端

- Java 17
- Spring Boot `3.3.5`
- Maven
- Spring Web
- Spring Validation / Jakarta Validation
- Spring Data JPA
- LangChain4j `1.15.0`
- langchain4j-open-ai
- MySQL Connector/J

### 数据库

- 默认数据库：MySQL
- 初始化 SQL：`backend/sql/voicecal.sql`
- 当前未配置 H2
- `dev`、`prod`、`test` profile 均使用 MySQL 配置

---

## 系统架构

```text
用户语音 / 文本输入
        ↓
前端 VoiceAssistantCard
        ↓
语音录制 / 文本输入
        ↓
后端语音识别 / AI 指令处理
        ↓
FastCommandRouter 或 LangChain4j Agent
        ↓
Calendar Tools
        ↓
日程 CRUD / 冲突检测 / 空闲时间查询 / ICS 导出
        ↓
MySQL
        ↓
前端日历视图、提醒、摘要、操作日志展示
```

项目采用前后端分离架构：

```text
frontend/
  React + Vite + TypeScript + Tailwind CSS + FullCalendar

backend/
  Java 17 + Spring Boot 3 + Spring Data JPA + LangChain4j + MySQL
```

---

## 核心功能说明

### 1. 语音输入

前端通过浏览器麦克风录音，使用 `MediaRecorder` 和 `Web Audio API` 完成音频采集与轻量语音状态检测。

录音完成后，音频会上传到后端语音识别接口。识别结果会自动填入输入框，并默认继续发送给 AI 日历助手处理。

相关接口：

```text
POST /api/voice/transcribe
POST /api/ai/speech/transcriptions
```

---

### 2. 文本输入兜底

即使语音识别不可用，用户也可以直接在文本框中输入自然语言指令。

文本指令会发送到：

```text
POST /api/ai/chat
```

---

### 3. 日程管理

后端提供完整的本地日程管理能力：

- 创建日程
- 查询日程列表
- 查询日程详情
- 更新日程
- 删除日程
- 查询今日日程
- 查询本周日程
- 按日期查询日程
- 按分类查询日程

---

### 4. AI Agent 与工具调用

项目通过 LangChain4j 的 Tool Calling 能力，将日历操作封装为可被大模型调用的工具。

当前 Calendar Tools 包括：

```text
listCalendarEvents
getCalendarEventById
createCalendarEvent
checkCalendarConflict
findFreeTime
exportCalendarEventsIcs
deleteCalendarEvent
updateCalendarEvent
```

大模型主要负责理解复杂自然语言指令，实际日历操作由后端工具完成。

---

### 5. FastCommandRouter 快速路由

对于安全、高频、确定性的查询类指令，系统会优先绕过大模型，直接调用后端服务返回结果。

支持的典型指令包括：

```text
今天安排查询
明天安排查询
本周日程查询
指定日期或时段的空闲时间查询
```

这样可以减少模型调用次数，降低响应延迟。

---

### 6. 冲突检测

创建或修改日程时，系统会检测新日程时间段是否与已有日程发生重叠。

相关接口：

```text
POST /api/calendar/events/conflicts
```

---

### 7. 空闲时间查询

用户可以查询某个时间范围内是否有空，例如：

```text
我明天下午有空吗？
```

后端会根据已有日程计算可用时间段。

相关接口：

```text
GET /api/calendar/events/free-time
```

---

### 8. 修改 / 删除确认机制

创建日程属于高频操作，在参数校验和冲突检测通过后可以直接执行。

修改和删除属于高风险操作，系统会要求用户确认后再执行，避免语音识别错误或意图理解错误造成误改、误删。

当前确认机制基于系统 Prompt 和工具方法实现，尚未引入独立的 pending action 状态机。

---

### 9. 提醒与最近提醒

`CalendarEvent` 中包含提醒相关字段：

```text
reminderMinutes
reminderTriggered
remindedAt
```

后端定时扫描到期提醒并更新状态，前端通过最近提醒模块展示提醒结果。

相关接口：

```text
GET /api/reminders/recent
```

---

### 10. 操作日志

系统会记录 AI / 语音命令执行过程，包括：

- 用户输入
- AI 回复
- 路由来源
- 执行状态
- 创建时间

相关接口：

```text
GET /api/logs/recent
```

---

### 11. 每日摘要

后端支持基于规则生成每日摘要，包括：

- 当天事件数量
- 忙碌分钟数
- 分类统计
- 最早日程
- 最晚日程

相关接口：

```text
GET /api/ai/daily-summary
```

---

### 12. ICS 导出

支持将单个日程或指定时间范围内的日程导出为 `.ics` 文件。

相关接口：

```text
GET /api/calendar/events/{id}/ics
GET /api/calendar/events/ics
```

---

## 项目结构

```text
voicecal-agent/
├── backend/
│   ├── pom.xml
│   ├── sql/
│   │   └── voicecal.sql
│   └── src/main/
│       ├── java/com/voicecal/
│       │   ├── VoiceCalApplication.java
│       │   ├── modules/ai/
│       │   ├── modules/calendar/
│       │   ├── modules/reminder/
│       │   └── modules/log/
│       └── resources/
│           ├── application.yml
│           ├── application-dev.yml
│           ├── application-prod.yml
│           └── application-test.yml
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── main.tsx
│       ├── App.tsx
│       ├── components/
│       │   ├── VoiceAssistantCard.tsx
│       │   ├── CalendarView.tsx
│       │   └── EventDetailPanel.tsx
│       ├── hooks/
│       │   └── useSpeechRecognition.ts
│       ├── lib/
│       │   └── apiClient.ts
│       └── services/
└── docs/
    ├── api.md
    ├── architecture.md
    └── google-calendar-sync-design.md
```

重点模块：

```text
前端入口：frontend/src/main.tsx、frontend/src/App.tsx
语音助手组件：frontend/src/components/VoiceAssistantCard.tsx
语音录音 Hook：frontend/src/hooks/useSpeechRecognition.ts
日历组件：frontend/src/components/CalendarView.tsx
后端启动类：backend/src/main/java/com/voicecal/VoiceCalApplication.java
日程模块：backend/src/main/java/com/voicecal/modules/calendar
AI 模块：backend/src/main/java/com/voicecal/modules/ai
提醒模块：backend/src/main/java/com/voicecal/modules/reminder
日志模块：backend/src/main/java/com/voicecal/modules/log
```

---

## 本地启动

### 环境要求

- Java 17
- Maven
- Node.js
- MySQL

> Vite `7.3.3` 对 Node.js 版本要求较高，建议使用 Node.js `20.19+` 或 `22.12+`。

---

### 1. 初始化数据库

先创建 MySQL 数据库，并执行初始化 SQL：

```bash
mysql -u root -p < backend/sql/voicecal.sql
```

也可以根据自己的 MySQL 配置手动执行 `backend/sql/voicecal.sql`。

---

### 2. 启动后端

```bash
mvn -f backend/pom.xml spring-boot:run
```

默认后端端口：

```text
8080
```

---

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

本地访问：

```text
http://localhost:5173
```

Vite 开发服务器会将 `/api` 代理到：

```text
http://localhost:8080
```

---

## 环境变量说明

### 数据库相关

```text
DEV_SERVER_PORT
DEV_MYSQL_URL
DEV_MYSQL_USERNAME
DEV_MYSQL_PASSWORD

TEST_SERVER_PORT
TEST_MYSQL_URL
TEST_MYSQL_USERNAME
TEST_MYSQL_PASSWORD

PROD_SERVER_PORT
PROD_MYSQL_HOST
PROD_MYSQL_PORT
PROD_MYSQL_DATABASE_NAME
PROD_MYSQL_USERNAME
PROD_MYSQL_PASSWORD
PROD_MYSQL_MAX_POOL_SIZE
PROD_MYSQL_MIN_IDLE
PROD_MYSQL_CONNECTION_TIMEOUT
PROD_MYSQL_VALIDATION_TIMEOUT
PROD_MYSQL_MAX_LIFETIME
PROD_MYSQL_IDLE_TIMEOUT
PROD_MYSQL_KEEPALIVE_TIME
```

### LLM / 大模型相关

```text
DASHSCOPE_API_KEY
QWEN_BASE_URL
QWEN_MODEL_NAME
QWEN_TEMPERATURE
QWEN_TIMEOUT_SECONDS
QWEN_MAX_RETRIES
```

### ASR / 语音识别相关

```text
QWEN_ASR_BASE_URL
QWEN_ASR_MODEL
QWEN_ASR_MODEL_NAME
QWEN_ASR_TIMEOUT_SECONDS
QWEN_ASR_MAX_AUDIO_BYTES
```

### 前端相关

```text
VITE_API_BASE_URL
```

### 提醒相关

```text
voicecal.reminder.scan-delay-ms
voicecal.reminder.initial-delay-ms
```

> 注意：不要将真实 API Key、数据库密码、Access Token 等敏感信息提交到公开仓库。

---

## 主要 API 接口

### 健康检查

```text
GET /api/health
```

### 日程 CRUD

```text
GET    /api/calendar/events
POST   /api/calendar/events
GET    /api/calendar/events/{id}
PUT    /api/calendar/events/{id}
DELETE /api/calendar/events/{id}
```

### 日程查询

```text
GET /api/calendar/events/today
GET /api/calendar/events/week
GET /api/calendar/events/by-date
```

### 冲突检测

```text
POST /api/calendar/events/conflicts
```

### 空闲时间查询

```text
GET /api/calendar/events/free-time
```

### 语音识别

```text
POST /api/voice/transcribe
POST /api/ai/speech/transcriptions
```

### AI 指令处理

```text
POST /api/ai/chat
```

### 提醒

```text
GET /api/reminders/recent
```

### 操作日志

```text
GET /api/logs/recent
```

### 每日摘要

```text
GET /api/ai/daily-summary
```

### ICS 导出

```text
GET /api/calendar/events/{id}/ics
GET /api/calendar/events/ics
```

---

## 数据库说明

当前项目默认使用 MySQL。

主要表：

```text
calendar_event
voice_command_log
```

### calendar_event

用于保存本地日程事件，包含：

- 标题
- 描述
- 开始时间
- 结束时间
- 地点
- 分类
- 状态
- 提醒时间
- 提醒触发状态

### voice_command_log

用于保存 AI / 语音命令操作日志，包含：

- 用户输入
- AI 回复
- 意图
- 工具信息
- 路由来源
- 成功状态
- 创建时间

当前 `dev` 和 `prod` profile 的 JPA 配置为：

```text
ddl-auto: validate
```

因此启动前需要先执行初始化 SQL。

---

## 当前限制

- 语音识别依赖浏览器麦克风权限和云端 ASR 服务。
- 公网部署语音输入需要 HTTPS，否则浏览器可能限制麦克风访问。
- 当前主要是本地日历，没有真实 Google Calendar 同步。
- 当前没有 Google OAuth 页面，也没有外部日历 token 存储。
- ASR 识别不准时需要改用文本输入兜底。
- 模型响应速度受网络和服务商可用性影响。
- 当前无用户体系和权限隔离，日程数据是全局本地数据。
- 前端传统表单创建日程尚未实现。
- 修改 / 删除确认机制目前基于 Prompt 和工具方法实现，没有独立 pending action 状态机。
- 公开仓库前需要确保配置文件中不包含任何真实密钥。

---

## 未来规划

- 接入 Google Calendar 同步。
- 支持实时流式语音识别。
- 优化移动端适配。
- 支持团队日历与多人协作。
- 增强系统级提醒推送。
- 支持多供应商 ASR / LLM 切换。
- 增加用户体系和权限隔离。
- 补充 Docker、Caddy、HTTPS 和 CI/CD 部署文档。

---

## 安全说明

本项目涉及大模型 API Key、ASR API Key、数据库账号密码等敏感配置。请使用环境变量或私有配置文件管理敏感信息，不要将真实密钥提交到公开仓库。

```text
不要提交：
- API Key
- Access Token
- 数据库密码
- 服务器私钥
- 生产环境配置文件
```
