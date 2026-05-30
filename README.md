# VoiceCal Agent

VoiceCal Agent 是一个语音驱动的 AI 日历助手 Demo 项目，支持用户通过语音或文本管理本地日程，并通过 Spring Boot 后端提供日程管理、冲突检测、空闲时间查询、每日摘要、提醒状态、操作日志和 ICS 导出等能力。

项目定位是本地演示和比赛路演原型：前端提供现代化 Dashboard 与语音助手入口，后端提供本地日历 API 和 LangChain4j Tool Calling 基础能力。Google Calendar 同步目前仅提供扩展设计文档，尚未真实接入 Google Calendar API 或 OAuth。

## 核心功能

- 语音输入与文本兜底输入：前端使用浏览器 Web Speech API，语音识别不可用时仍可文本输入。
- AI 对话入口：`POST /api/ai/chat` 统一处理用户指令；无真实模型时返回 fallback 文案。
- 日程 CRUD：创建、查询、更新、删除本地日程。
- 日程冲突检测：按 overlap 规则检测时间段是否与已有日程重叠。
- 空闲时间查询：查询给定时间段内满足最小时长的空闲区间。
- 每日摘要：返回当日事件数、忙碌分钟数、分类统计、首尾重点日程和摘要文案。
- 危险操作确认机制：AI 删除或修改日程前先创建 PendingAction，用户确认后才执行。
- 日历可视化：前端使用 FullCalendar 展示月视图和周视图。
- 操作日志：记录用户输入、AI 回复、会话 ID、成功状态和创建时间。
- 提醒状态：支持 `reminderMinutes`、`reminderTriggered`、`remindedAt` 字段和最近提醒查询。
- ICS 导出：后端支持单个日程和时间范围导出 `.ics` 文件，前端事件详情可下载单个事件 ICS。
- 日程分类：支持 `WORK`、`STUDY`、`LIFE`、`MEETING`、`INTERVIEW`、`OTHER` 分类和标题关键词推断。
- Google Calendar 同步扩展设计：已在文档中设计未来 OAuth、Provider 抽象、映射表和同步策略，不是真实同步能力。

## 技术栈

后端：

- Java 17
- Spring Boot 3
- Maven
- Spring Web
- Spring Validation / Jakarta Validation
- Spring Data JPA
- H2 / MySQL profile
- LangChain4j

前端：

- React
- Vite
- TypeScript
- Tailwind CSS
- FullCalendar
- Web Speech API

## 仓库结构

```text
voicecal-agent/
├── backend/   # Spring Boot 后端，包含日程、AI、日志、提醒等模块
├── frontend/  # React + Vite 前端 Dashboard
├── docs/      # 架构、API、Demo 脚本和扩展设计文档
└── README.md  # 项目说明与快速启动
```

## 后端启动

要求：JDK 17、Maven。

从仓库根目录启动默认 H2 profile：

```powershell
mvn.cmd -f backend\pom.xml spring-boot:run
```

或进入后端目录：

```powershell
cd backend
mvn spring-boot:run
```

默认配置：

- 服务端口：`8080`
- 默认 active profile：`h2`
- H2 JDBC URL：`jdbc:h2:mem:voicecal;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- H2 Console：`/h2-console`
- H2 profile 使用 `ddl-auto: update`，用于本地开发和 Demo 自动建表。

MySQL profile 已提供基础配置，可通过以下方式启用：

```powershell
mvn.cmd -f backend\pom.xml spring-boot:run -Dspring-boot.run.profiles=mysql
```

MySQL 本地连接信息请按本机环境调整，不要提交真实密码、token 或其他 secret。

## 前端启动

要求：Node.js 与 npm。

```powershell
cd frontend
npm install
npm run dev
```

构建：

```powershell
npm run build
```

前端默认通过 Vite 代理访问后端 `/api`。如果使用自定义后端地址，可按前端服务实现配置 `VITE_API_BASE_URL`，不要提交 `.env`。

## AI 模型配置

当前后端通过 LangChain4j 注册了 `CalendarEventTools`。`VoiceCalAssistant` 仅在存在 `ChatModel` Bean 时创建。

未配置真实模型时，`/api/ai/chat` 会返回 fallback 文案：

```text
AI provider is not configured yet. VoiceCal calendar tools are registered and ready for use when a chat model is configured.
```

如需启用 Qwen 模型，在本地 shell 中配置环境变量后启动后端：

```powershell
$env:DASHSCOPE_API_KEY="your-api-key"
$env:QWEN_MODEL_NAME="qwen3.7-max"
mvn.cmd -f backend\pom.xml spring-boot:run
```

默认使用 DashScope OpenAI 兼容地址 `https://dashscope.aliyuncs.com/compatible-mode/v1`。
如需覆盖，可设置 `QWEN_BASE_URL`。不要把真实 API Key 写入代码、配置文件、`.env` 或提交记录。

本项目不要求提交任何 API Key。接入真实模型时应通过本地环境变量或私有配置完成，并确保不把 key、token、secret 写入仓库。

## 核心 API 摘要

详细说明见 [docs/api.md](docs/api.md)。

- `GET /api/health`：健康检查
- `POST /api/calendar/events`：创建日程
- `GET /api/calendar/events`：查询日程列表，可选 `category`
- `GET /api/calendar/events/{id}`：查询日程详情
- `PUT /api/calendar/events/{id}`：更新日程
- `DELETE /api/calendar/events/{id}`：删除日程
- `GET /api/calendar/events/today`：查询今日日程
- `GET /api/calendar/events/week`：查询本周日程
- `GET /api/calendar/events/by-date`：查询指定日期日程
- `POST /api/calendar/events/conflicts`：检测日程冲突
- `GET /api/calendar/events/free-time`：查询空闲时间
- `GET /api/calendar/events/{id}/ics`：导出单个日程 ICS
- `GET /api/calendar/events/ics`：按时间范围导出 ICS
- `POST /api/ai/chat`：AI 对话
- `GET /api/ai/daily-summary`：每日摘要
- `GET /api/pending-actions`：查询待确认操作
- `POST /api/pending-actions/{id}/confirm`：确认待确认操作
- `POST /api/pending-actions/{id}/cancel`：取消待确认操作
- `GET /api/logs/recent`：查询最近操作日志
- `GET /api/reminders/recent`：查询最近已触发提醒

## Tool Calling 能力

当前 `CalendarEventTools` 暴露的核心能力包括：

- 查询日程列表
- 根据 ID 查询日程
- 创建日程
- 检测日程冲突
- 查询空闲时间
- 创建待确认删除操作
- 创建待确认更新操作
- 确认待确认操作
- 取消待确认操作

AI Tool 不直接暴露危险的删除或修改执行入口，而是通过 PendingAction 确认机制保护高风险操作。

## 文档

- [架构文档](docs/architecture.md)
- [API 文档](docs/api.md)
- [Demo 脚本](docs/demo-script.md)
- [最终交付检查清单](docs/final-checklist.md)
- [Google Calendar 同步扩展设计](docs/google-calendar-sync-design.md)

Demo video: TBD
