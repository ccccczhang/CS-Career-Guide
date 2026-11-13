# CS Career Guide Android

现有 Vue 前端的 Compose 迁移工程。2026-11-13 已完成认证、简历、正式目录和岗位/建议查询，其他业务按路由分阶段接入。

Phase 1–4 的 UI/Mock 基线已通过模拟器流程测试；真实接口按 [后端接入计划](docs/backend-integration-plan.md) 推进。

## 当前范围

- 覆盖前端现有 18 个路由。
- 5 个底部入口：首页、规划、求职、学习、我的。
- 真实登录、注册、profile、退出和 401 会话清理。
- Repository 边界接入 OkHttp；Screen 不直接请求网络。
- 简历、目录、岗位和前辈经验已接服务端；练习、面试等未迁移模块保留阶段性数据。
- 图片不是当前 UI/Mock 验收条件。
- 职业路径、个人自述、24 项技能、职业选择和规划报告。
- 职位、红黑榜、前辈经验、求职安全和本地经验提交。
- 七章节简历编辑/预览、本地保存和假导出。
- 四风格 Mock 面试、计时、暂停、结束、复盘和假语音状态。
- 岗位与企业目录、题库查询、来源/许可信息和空结果状态。
- 简历 JD 分析、原文证据、建议选择和确认应用入口。
- 题库检索演示、练习记录、逐题反馈和总结；管理员入口暂时隐藏。
- 个人中心资料编辑、职业推荐、简历摘要、复盘与练习入口。

完整需求与路由矩阵见 [migration-to-android.md](migration-to-android.md)。

各阶段范围、状态和 HTTP 测试期约定见 [后端接入计划](docs/backend-integration-plan.md)；接口边界见 [桥接说明](docs/bridge-integration.md)。

## 本地验证

```powershell
$env:ANDROID_HOME = 'C:\Users\Prologue\AppData\Local\Android\Sdk'
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug "-PapiBaseUrl=<debug-origin>"
.\gradlew.bat connectedDebugAndroidTest "-PapiBaseUrl=<debug-origin>" "-Pandroid.testInstrumentationRunnerArguments.testUsername=<test-account>" "-Pandroid.testInstrumentationRunnerArguments.testPassword=<test-password>"
```

APK 输出在 `app/build/outputs/apk/debug/`。

`API_BASE_URL` 不带 `/api`。当前产品测试阶段默认 `ALLOW_CLEARTEXT=true`，仅在网络配置中放行已确认测试主机；取得 HTTPS 后设置 `-PallowCleartext=false` 恢复发布门禁。

2026-11-13 本地结果：41 项 JVM 测试和 6 项 Android 模拟器测试通过；服务器验证认证、简历保存、正式目录和岗位/建议查询；Debug/HTTP Release APK 构建通过；lint 0 error、10 个依赖版本 warning。

## 当前未接入

正式练习、模型、SSE、麦克风、ASR/TTS、PDF 和评估仍按后续阶段接入。当前 HTTP 仅用于产品测试构建，不标记为 HTTPS 或生产安全验收。
