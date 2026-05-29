# Google Calendar 同步扩展设计

## 1. 背景与目标

VoiceCal Agent 当前使用本地日历数据作为核心数据源，已能支持语音助手围绕本地日程进行创建、查询、摘要和可视化展示。

真实用户通常还会使用 Google Calendar、Outlook、Apple Calendar 等外部日历系统。未来如果支持外部日历同步，可以让 VoiceCal Agent 更接近用户真实工作流，减少重复维护多个日历的成本。

本 PR 只补充 Google Calendar 同步扩展设计，不实现真实 Google Calendar API 接入，不实现 OAuth，不提交任何 Google Cloud 凭证。

## 2. 当前本地日历能力

当前项目已实现以下本地日历能力：

- 本地 `CalendarEvent` 存储
- 日程 CRUD
- 日程冲突检测
- 空闲时间查询
- 每日摘要
- 日程提醒状态记录
- ICS 日历文件导出
- 前端日历可视化视图

这些能力仍然以本地日历作为默认数据源。外部日历同步应作为扩展能力接入，而不是替换当前主流程。

## 3. 目标架构

未来可以引入 `CalendarProvider` 抽象，用统一接口屏蔽本地日历和外部日历系统的差异。

建议接口能力：

- `create`
- `update`
- `delete`
- `get`
- `list`
- `export`

当前实现：

- `LocalCalendarProvider`

未来实现：

- `GoogleCalendarProvider`
- `OutlookCalendarProvider`

设计原则：

- 业务层不直接依赖 Google Calendar SDK 或其他外部 SDK。
- 本地日历仍作为默认 provider。
- 外部 provider 只负责适配外部日历 API，不承载 VoiceCal 的业务规则。
- 冲突检测、危险操作确认、摘要等业务能力仍优先围绕本地领域模型组织。

## 4. Google Calendar OAuth2 授权流程

未来 Google Calendar 接入需要 OAuth2 授权流程：

1. 用户点击连接 Google Calendar。
2. 前端跳转到 Google OAuth consent screen。
3. 用户授权最小必要 calendar scope。
4. Google 回调后端并携带 authorization code。
5. 后端使用 authorization code 交换 access token 和 refresh token。
6. 后端加密保存 refresh token。
7. 后续同步时，后端使用 refresh token 换取新的 access token。

注意：

- 当前 PR 不实现 OAuth。
- 当前 PR 不提交 client id、client secret、access token、refresh token。
- token 存储和刷新策略需要在后续安全设计中单独实现和测试。

## 5. 本地事件与外部事件映射

外部日历系统会为事件生成自己的 ID 和版本标记。本地事件和外部事件需要一张映射表关联，否则无法判断某个本地事件是否已同步、对应哪个外部事件、同步状态是否失败。

建议表名：

`external_calendar_mapping`

字段建议：

| 字段 | 说明 |
| --- | --- |
| `id` | 映射记录 ID |
| `localEventId` | 本地 `CalendarEvent` ID |
| `provider` | 外部日历提供方，例如 `GOOGLE`、`OUTLOOK` |
| `externalCalendarId` | 外部日历 ID |
| `externalEventId` | 外部事件 ID |
| `syncStatus` | 同步状态 |
| `etag` | 外部事件版本标记，用于冲突检测 |
| `lastSyncedAt` | 最近同步时间 |
| `createdAt` | 创建时间 |
| `updatedAt` | 更新时间 |

`syncStatus` 建议枚举：

- `SYNCED`
- `PENDING_CREATE`
- `PENDING_UPDATE`
- `PENDING_DELETE`
- `FAILED`

## 6. 同步策略

### 单向同步

单向同步指从 VoiceCal 本地日历同步到 Google Calendar。

适合 MVP 初期，复杂度较低：

- 本地创建日程后，在 Google Calendar 创建事件。
- 本地更新日程后，更新对应 Google 事件。
- 本地删除日程后，删除或取消对应 Google 事件。

### 双向同步

双向同步指本地和外部日历互相同步变更。

复杂度更高，需要处理：

- 外部事件新增后拉取到本地。
- 外部事件更新后合并到本地。
- 外部事件删除后同步本地状态。
- 本地和外部同时修改同一事件时的冲突处理。

### 推荐阶段

建议未来按阶段推进：

1. Phase 1：手动导出 ICS。
2. Phase 2：单向同步本地事件到 Google Calendar。
3. Phase 3：基于增量同步的双向同步。

## 7. 冲突处理策略

外部日历同步可能产生以下冲突：

- 本地和 Google 同时修改同一事件。
- Google 事件被删除，本地仍存在。
- 本地事件被删除，Google 仍存在。
- 外部事件同步到本地后产生时间段冲突。

建议策略：

- 使用 `etag` 或外部事件 `updatedAt` 判断版本变化。
- 使用本地 `updatedAt` 判断本地事件是否在上次同步后被修改。
- 默认不自动覆盖用户修改。
- 有风险的删除或覆盖操作进入 PendingAction 确认机制。
- 时间段冲突复用已有冲突检测能力，提示用户确认处理方式。

## 8. 增量同步设计

Google Calendar 支持使用 `syncToken` 做增量同步。未来可以记录每个用户或每个外部日历的同步游标。

建议流程：

1. 首次连接后执行全量同步。
2. 保存 Google Calendar 返回的 `syncToken`。
3. 后续同步使用 `syncToken` 拉取增量变化。
4. 记录 `lastSyncedAt` 作为辅助审计字段。
5. 如果 `syncToken` 失效，回退到全量同步。
6. 对删除事件使用 tombstone 或本地状态标记，避免误恢复已删除事件。

当前项目不实现 `syncToken`，这里只保留未来设计。

## 9. 安全性考虑

外部日历同步会引入敏感凭证和用户隐私数据，必须单独处理安全问题。

要求：

- token 必须加密存储。
- 不把 token、authorization code、client secret 写入日志。
- 不提交 `.env` 或任何 Google Cloud 凭证。
- OAuth scope 使用最小权限。
- 用户可以撤销授权。
- 支持 refresh token 轮换和失效处理。
- 错误响应不暴露敏感信息。
- 操作日志中只记录必要摘要，不记录完整外部 API 凭证或敏感 payload。

## 10. 失败重试与限流

外部日历 API 可能出现网络失败、服务限流或权限失效。

建议策略：

- 对临时网络错误使用 retry。
- 使用指数退避，避免触发更严重限流。
- 同步失败时将映射或任务状态记录为 `FAILED`。
- 同步失败不阻塞本地日历 CRUD 主流程。
- 对权限失效类错误提示用户重新授权。
- 对长期失败的同步任务提供人工重试入口。

## 11. 与现有功能的关系

未来 Google Calendar 同步应与现有能力保持清晰边界：

- 冲突检测：外部事件拉取到本地前后都可以复用现有 overlap 规则。
- 空闲时间查询：未来可以基于本地事件和已同步外部事件统一计算。
- PendingAction 确认机制：删除、覆盖、冲突合并等危险操作仍需用户确认。
- 提醒机制：本地提醒状态和 Google Calendar 原生提醒需要明确优先级，避免重复提醒。
- ICS 导出：仍作为无授权、低成本的导出方式保留。
- 操作日志：同步结果可记录摘要，但不得记录 token 或敏感凭证。

## 12. 本 PR 范围

本 PR 只提供 Google Calendar 同步扩展设计文档。

本 PR 不实现：

- Google Calendar API 接入
- OAuth 页面或授权回调
- Google Cloud 凭证配置
- GoogleCalendarProvider 真实 API 调用
- 同步调度任务
- 增量同步
- 双向同步
- token 加密存储
- 前端 UI 改动

当前本地日历 CRUD、冲突检测、空闲时间查询、提醒、ICS 导出和可视化主流程不受本 PR 影响。
