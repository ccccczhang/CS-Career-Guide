# CS Career Guide Android UI/Mock

现有 Vue 前端的 Compose UI/Mock 迁移工程，当前完成 Phase 4 本地实现。

Phase 1–4 已通过 Android 模拟器流程测试，等待真实后端桥接、真机视觉与无障碍验收。

## 当前范围

- 覆盖前端现有 18 个路由。
- 5 个底部入口：首页、规划、求职、学习、我的。
- 本地假登录、假注册和退出。
- Repository Mock，无网络请求。
- 图片不是当前 UI/Mock 验收条件。
- 职业路径、个人自述、24 项技能、职业选择和规划报告。
- 职位、红黑榜、前辈经验、求职安全和本地经验提交。
- 七章节简历编辑/预览、本地保存和假导出。
- 四风格 Mock 面试、计时、暂停、结束、复盘和假语音状态。
- 岗位与企业目录、题库查询、来源/许可信息和空结果状态。
- 简历 JD 分析、原文证据、建议选择和确认应用入口。
- 题库检索演示、练习记录、逐题反馈、总结和管理员评估。
- 个人中心资料编辑、职业推荐、简历摘要、复盘与练习入口。

完整需求与路由矩阵见 [migration-to-android.md](migration-to-android.md)。

后续真实接口接入要求见 [桥接说明](docs/bridge-integration.md)。代码内可搜索 `TODO(bridge)` 定位接入点。

## 本地验证

```powershell
$env:ANDROID_HOME = 'C:\Users\Prologue\AppData\Local\Android\Sdk'
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

APK 输出在 `app/build/outputs/apk/debug/`。

2026-11-12 本地结果：27 项单元测试和 4 项 Android 模拟器测试通过；同一组 UI 测试在约 `320dp` 宽、`1.3x` 字体缩放下再次通过。Debug APK 构建通过，lint 0 error。

## 当前未接入

真实后端、Token 持久化、头像上传、真实 PDF、模型、SSE、麦克风、ASR/TTS 和跨设备数据持久化均未接入。当前 APK 仍只用于 UI/Mock 验收。
