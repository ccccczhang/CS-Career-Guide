# Android 真实后端接入计划

**更新日期**：2026-11-13

**接口依据**：仓库根目录的 [App API 文档](../../docs/app-api.md)

**交付边界**：[Android 桥接说明](bridge-integration.md)

**当前状态**：阶段 1、2 完成，阶段 3 retrieve 已提交；阶段 4 文字 SSE 与复盘生成进行中

## 1. 当前配置约定

- `BuildConfig.API_BASE_URL` 只保存 `scheme://host[:port]`，不带 `/api`；`BridgeRoutes.API_PREFIX` 负责拼接业务路径。
- 当前服务器没有 HTTPS 证书，产品测试阶段临时允许 HTTP。默认 `ALLOW_CLEARTEXT=true`，通过 `-PapiBaseUrl=http://<test-host>` 和 `-PreleaseApiBaseUrl=http://<test-host>` 构建。
- Android 网络明文只放行已确认测试主机；不会使用 `cleartextTrafficPermitted=true` 放开所有域名。取得 HTTPS 后应设置 `-PallowCleartext=false`，移除测试主机白名单并恢复正常 HTTPS 门禁。
- `0.0.0.0:8000` 是后端监听地址，不是设备侧 URL。Android 模拟器访问本机后端应使用 `-PapiBaseUrl=http://10.0.2.2:8000`。
- 模拟器联调已确认服务器测试根地址可注入：`.\gradlew.bat installDebug -PapiBaseUrl=http://123.207.252.243 -PallowCleartext=true`。服务器地址只进入本地生成的 `BuildConfig`，不写入源码或仓库配置。
- Token 和当前用户缓存使用 `EncryptedSharedPreferences`。401 和退出清 Token、用户及界面状态；403 不清 Token。
- 管理员能力尚未开通，管理员入口暂时隐藏。

## 2. 五阶段范围

| 阶段 | 范围 | 状态 | 完成门禁 |
| --- | --- | --- | --- |
| 1. 基础设施与认证 | OkHttp、BuildConfig、错误模型、加密会话、登录/注册/profile/退出 | 完成 | MockWebServer、服务器登录/profile/logout、401 清理、退出清理和模拟器回归通过 |
| 2. 简历、目录与查询 | 简历读取/保存、头像路由、目录分页筛选、岗位与学长建议查询、简历分析路由与待开发状态 | 完成 | 空库与错误分离；来源/权限/演示字段保留；保存成功刷新完整简历；501 不伪装成功 |
| 3. 正式练习 | retrieve、创建会话、详情、回答、反馈恢复/重试、finish、总结重试、记录分页 | retrieve 已提交，其余待接入 | request_id 持久化，网络重发同键同内容，409/502/504 按详情恢复 |
| 4. 面试、语音与剩余能力 | SSE、复盘、ASR PCM、TTS、职业推荐/规划/历史 | 进行中：文字 SSE 与复盘生成 | `[DONE]` 后继续读 `end`；断流/取消可观测；真实模型和语音只按服务器授权验证 |
| 5. 回归与发布 | 双账号隔离、模拟器、lint、Debug/Release 测试构建、证据汇总 | 待开始 | 单元、MockWebServer、服务器和模拟器通过；HTTP 明确标记为测试期 |

## 3. 阶段 1 记录

已实现真实登录、注册、profile 读写、退出和会话恢复；错误模型保留 `httpStatus/businessCode/message/details/fieldErrors/retryable/authExpired`。Exit 先清本地状态，再尽力撤销远端 Token，避免退出重登竞态。

验证结果：

- JVM 37 项通过，0 失败。
- Android 模拟器 5 项通过，0 失败。
- 服务器登录 200、profile 200、退出 200，退出后旧 Token 为 401。
- Debug APK 构建通过；lint 0 error。
- 未调用真实模型、TTS 或 ASR。

## 4. 阶段 2 验收清单

- 简历 `GET /api/resumes/my/`：200 映射完整对象，404 视为尚未创建。
- 简历保存：首次 POST，已有简历 PUT；成功后用响应覆盖本地编辑状态。
- 目录：jobs/companies/questions 使用服务端分页和对应筛选，空库显示真实空结果。
- 查询：计算机岗位、企业职位、学长建议按实际响应映射；空数据不当作错误。
- 简历分析和建议应用：连接正式路由；后端 501 或缺少业务实现时显示“待开发”，不调用 Mock 成功。
- 头像路由和服务端字段保留；没有 UI 操作时不伪造上传成功。
- HTTP 测试构建允许已确认测试主机；Release 测试构建同样使用 `ALLOW_CLEARTEXT`，但不得标记为生产安全验收。

## 5. 阶段 2 记录

记录日期：2026-11-13。

服务器只读/能力探测：

- 三类正式目录均返回 200，当前数据为空。
- 计算机岗位、职业分类、旧企业列表和招聘会返回 200；`company-info` 当前返回 500。
- 学长建议和职业路径/问卷返回 200。
- `test_b` 首次简历 POST 返回 200，随后 GET 返回同一完整简历对象。
- 简历分析 POST 返回 501 `FEATURE_NOT_IMPLEMENTED`。

已实现：

- 简历 404 映射为“尚未创建”，首次保存 POST、已有简历 PUT，保存后使用完整响应覆盖编辑状态。
- 简历头像上传/删除路由已登记并实现，尚无头像 UI 操作。
- jobs/companies/questions 使用正式分页和类型对应筛选；页面支持上一页/下一页、空库、错误和来源/许可/演示字段。
- 计算机岗位和学长建议已接服务端查询；搜索条件发往服务端。
- 简历分析和建议应用已接正式路由，并携带调用方 request_id；501 显示待开发。
- 目录、企业/岗位和简历页的界面说明已改为真实能力状态。

验证结果：

- JVM 41 项通过，0 失败。
- Android 模拟器 6 项通过，0 失败；包含服务器认证、简历保存/读取、目录、岗位、建议和现有 UI 回归。
- 真实服务器 `test_b` 简历保存/读取通过。
- Debug APK 和 HTTP Release APK 构建通过；设置 `-PallowCleartext=false` 后，HTTP Release 会按预期被拒绝。
- lint 0 error，10 个依赖/Gradle 版本 warning。

## 6. 阶段 3 记录

记录日期：2026-11-13。

### 6.1 主线第 1 项：正式题库 retrieve

契约已按 [App API 文档](../../docs/app-api.md) 第 6.1 节核对：

- 路由：`POST /api/career-platform/questions/retrieve/`；`BridgeRoutes.QUESTION_RETRIEVE` 与文档一致。
- 请求：`role`、`topic`、`difficulty`、`job_id`、`jd_text`、`top_k`、`request_id`。原适配器中的 `query` 不属于正式接口，已移除；`job_id` 与 `jd_text` 互斥。
- 响应：顶层 `count`、`retrieval_id`、`message`、`results`；每项保留 `question_id`、`question`、`key_points`、`evidence`、`score`、`origin`、筛选字段、来源字段和许可字段。
- 空结果为 HTTP 200 的成功态，界面显示服务端提示，不补入 Mock 题目。

映射及固定样例验证：

| 服务端字段 | Android 字段 | 验证 |
| --- | --- | --- |
| `question_id` | `PracticeQuestion.id` | MockWebServer 断言 UUID |
| `question` | `PracticeQuestion.question` | MockWebServer 断言题干 |
| `key_points[]` | `PracticeQuestion.keyPoints` | MockWebServer 断言逐项内容 |
| `score` | `PracticeQuestion.matchScore` | MockWebServer 断言 `0.6` |
| `evidence[].source_id/field_path/quote` | `QuestionEvidence` 三字段 | MockWebServer 断言顺序和逐字段内容 |
| `origin/source/source_url/permission_status/collected_at` | 同名领域字段 | MockWebServer 断言来源、许可和空采集时间 |
| `count/retrieval_id/message/results` | `PracticeRetrieval` | Repository 与 MockWebServer 断言回执和结果 |

实现结果：

- UI → Repository → `HttpCareerBridge` 已接通正式 retrieve；界面文案不再标记为检索演示。
- 每次新检索生成 UUID `request_id`；网络失败后重试复用同一键，成功后清空键并保留 `retrieval_id`。
- 401 继续走统一会话清理；403 不清 Token。

验证结果：

- JVM：43 项通过，0 失败；其中 Repository 映射 1 项、MockWebServer 契约/字段映射 1 项。
- 服务器：`test_a` 登录后 retrieve 返回 HTTP 200、`count=0`、非空 `retrieval_id` 和调整条件提示。当前正式题库无数据，因此真实响应没有可核对的结果项；结果项字段以 API 文档和 MockWebServer 固定样例验证，待题库导入后再做真实字段抽查。
- 模拟器：连接服务器根地址后，阶段 3 instrumented test 1 项通过；全量 Android 测试 7 项通过，0 失败。
- 构建与静态检查：Debug APK 安装成功，`BuildConfig.API_BASE_URL` 为测试根地址且 `ALLOW_CLEARTEXT=true`；lint 0 error、10 个依赖/Gradle 版本 warning。
- 未调用真实模型、TTS 或 ASR，未修改数据库。

### 6.2 底部导航个人页路由修复

- 问题：一级 tab 导航同时启用 `saveState/restoreState`，旧 tab 子栈可能在再次进入“我的”时被恢复，导致个人页无法稳定到达；首页头像入口与底部入口共用该导航函数。
- 修复：一级 tab 导航保留 `popUpTo(Routes.HOME)` 和 `launchSingleTop`，移除 tab 状态保存/恢复，使“我的”每次明确落到 `Routes.USERS` 根页面。页面业务状态仍由各 ViewModel 保存，不依赖 NavController 的子栈恢复。
- 回归：新增个人页进入练习记录后返回、跨多个 tab、首页头像入口、系统返回和 Activity 重建场景。

验证结果：

- 专项 Android 导航测试 4 项通过，0 失败。
- 全量 JVM 43 项、Android 10 项通过，0 失败；lint 0 error。

## 7. 阶段 4 记录

记录日期：2026-11-13。

### 7.1 自由面试文字 SSE

- 路由：`POST /api/ai/llm/chat/`，使用 `Authorization: Token <token>`、JSON 请求和 `Accept: text/event-stream`。
- 请求：`message`、`mode`、`interview_style`、`session_id`、`enable_tts`。当前文字面试显式发送 `enable_tts=false`，不把语音开关冒充 TTS。
- 解析：按空行分帧，按 UTF-8 增量解码；支持单帧跨网络块、一个网络块多帧、CRLF、多行 `data:` 和末尾残帧。
- 完成：收到 `[DONE]` 后继续读取 `end`；只有 `[DONE]` 和带非空 `session_id` 的 `end` 都出现才判定完整。断流、缺少 `end` 或 error 帧保留已收到文字并显示可恢复失败。
- 超时：普通请求继续使用短超时；SSE 使用独立客户端，读超时 120 秒、总调用不固定为 30 秒，协程取消会关闭 OkHttp Call。
- ViewModel：开始面试和提交回答使用真实流；chunk 按顺序追加到同一面试官消息。暂停取消当前请求，不覆盖已收到的部分文本。
- 复盘：结束面试后使用同一聊天接口的 `interview_review` 模式生成真实文字复盘；当前暂不展示后端结构化评分，也不调用旧 Mock 评分。

验证结果：

- JVM：45 项通过，0 失败；新增完整 SSE 和断流保留部分文本两项。完整 SSE 使用 MockWebServer 逐字节限速发送，覆盖中文 UTF-8 跨块、CRLF 和 `[DONE] -> end` 顺序。
- 服务器短连通：HTTP 200、`text/event-stream; charset=utf-8`，收到 chunk、`[DONE]` 和非空 `session_id`；未收到 error 帧。
- 模拟器：真实服务器完成开始面试、等待完整流结束和生成复盘，专项流程 1 项通过。
- 语音输入、TTS 播放、复盘持久化、历史列表和 ASR 尚未接入。
