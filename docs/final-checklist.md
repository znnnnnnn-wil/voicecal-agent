# VoiceCal Agent 最终交付检查清单

## 1. 后端检查

- [x] 执行 `mvn.cmd -f backend\pom.xml clean test`
- [x] Spring Boot 测试上下文可正常加载
- [x] MySQL profile 可用于本地 Demo 和自动化测试
- [x] 测试环境使用独立 MySQL 测试库
- [x] 核心 API 路径与 Controller 保持一致
- [x] 不依赖真实外部 LLM API
- [x] 未提交硬编码 API Key、token、secret
- [x] 全局异常处理隐藏未预期异常堆栈

重点覆盖能力：

- [x] Calendar CRUD
- [x] 今日 / 本周 / 指定日期查询
- [x] Daily Summary
- [x] 冲突检测
- [x] 空闲时间查询
- [x] AI 修改和删除日程
- [x] VoiceCommandLog
- [x] Reminder 扫描与最近提醒查询
- [x] ICS 单事件导出与范围导出

## 2. 前端检查

- [x] 执行 `cd frontend && npm install`
- [x] 执行 `npm run build`
- [x] 项目当前未配置 `npm run lint` 脚本
- [x] 后端不可用时前端有 demo fallback 状态
- [x] 语音识别不支持时有文本兜底
- [x] 日历视图、事件详情、摘要、日志和提醒区域可构建通过
- [x] ICS 导出按钮不会破坏 TypeScript 构建

建议人工联调时继续确认：

- [ ] `npm run dev` 可启动
- [ ] 页面无明显控制台错误
- [ ] 移动端无明显横向溢出

## 3. 联调场景

- [ ] 语音或文本创建日程
- [ ] 查询日程
- [ ] 查询每日摘要
- [ ] 冲突检测
- [ ] 空闲时间查询
- [ ] AI 修改和删除日程
- [ ] 操作日志展示
- [ ] 提醒状态展示
- [ ] 单个日程 ICS 导出
- [ ] 时间范围 ICS 导出
- [ ] 日程分类展示

## 4. 文档检查

- [x] README 启动命令与当前项目一致
- [x] `docs/api.md` 路径与当前 Controller 保持一致
- [x] `docs/demo-script.md` 可用于录制 Demo
- [x] `docs/google-calendar-sync-design.md` 明确 Google Calendar 是未来扩展设计
- [x] 文档未写已支持真实 Google Calendar 同步
- [x] 文档未写已支持 OAuth
- [x] 文档未写已支持真实推送或 WebSocket

## 5. Git 交付检查

提交前请执行：

```powershell
git status --short
```

确认不要提交：

- `.idea/`
- `target/`
- `*.iml`
- `node_modules/`
- `dist/`
- `.env`
