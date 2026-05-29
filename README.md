# VoiceCal Agent

VoiceCal Agent 是一个基于 Java Spring Boot 与 LangChain4j 的语音日历助手。项目目标是让用户通过自然语言和语音交互完成日程创建、查询、修改、删除、提醒设置、冲突检测、空闲时间查询、每日摘要和 ICS 导出。

## 项目定位

VoiceCal Agent 面向日历管理场景，定位为一个适合比赛路演和 Demo 展示的 AI 助手产品。项目重点关注语音交互体验、可靠的日程操作、清晰的反馈机制，以及现代化、直观的前端展示效果。

## 技术栈规划

### 后端

- Java 17
- Spring Boot 3
- Maven
- Spring Data JPA
- 本地开发使用 H2
- 部署或集成测试可使用 MySQL profile
- 使用 LangChain4j 接入大模型能力
- 通过 Tool Calling 调用日历操作能力
- 统一接口响应格式
- 全局异常处理

### 前端

- React
- Vite
- TypeScript
- Tailwind CSS
- Dashboard 风格的 AI 助手界面
- 支持浏览器语音输入能力
- 提供文本输入兜底方案

## 功能规划

- 语音创建日程
- 文本兜底输入
- 日程查询、修改和删除
- 提醒设置
- 日程冲突检测
- 空闲时间查询
- 每日摘要
- ICS 导出
- AI 回复展示
- 日历可视化
- 今日和本周日程概览
- 操作日志展示

## 仓库结构

```text
voicecal-agent/
+-- README.md
+-- docs/
|   +-- pr-plan.md
+-- backend/
+-- frontend/
```

- `backend/`：后续用于存放 Spring Boot 与 LangChain4j 后端服务。
- `frontend/`：后续用于存放 React、Vite、TypeScript 与 Tailwind CSS 前端应用。
- `docs/`：用于存放项目规划、PR 拆分和其他工程文档。

## 当前状态

当前仓库仅完成 monorepo 基础骨架初始化。尚未初始化 Spring Boot 项目，尚未初始化前端项目，也尚未实现业务逻辑、接口契约、数据库结构或 AI 集成能力。
