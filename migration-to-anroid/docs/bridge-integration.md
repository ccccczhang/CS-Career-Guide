# Android 后续桥接说明

**核对日期**：2026-11-12  
**适用工程**：`C:\Users\Prologue\develop\work\migration-to-anroid`  
**当前阶段**：Compose UI/Mock 已完成，真实后端尚未接入

本文说明 Android 从本地 Mock 切换到真实接口时必须补齐的内容。接口行为的唯一后端依据是 CS-Career-Guide 仓库的 [App API 文档](C:/Users/Prologue/develop/CS-Career-Guide/docs/app-api.md)；本文只定义 Android 侧的交付边界和验收前置条件。

## 1. 当前边界

- 所有页面依赖 `AppRepository`、`CareerPrepViewModel`、`PlanningViewModel` 和 `WorkspaceViewModel`，Screen 不直接请求网络。
- 登录、个人资料、简历、练习记录、评估和面试仍保存在当前进程内存；重启应用后不保证恢复。
- 当前路由与 UI 不新增 Web 端不存在的业务；`CareerBridgeContract.kt` 只登记未来 HTTP 路径和适配器边界。
- 真实接口、Token、模型、ASR/TTS、PDF、头像上传和跨设备持久化均未验收。
- 不把 Mock 成功当作真实接口成功；桥接后仍需分别记录本地测试、联调测试和真机验收。

## 2. 后端必须提供

### 2.1 环境与认证

1. 提供开发、预发布和生产的 API 根地址。Android 不硬编码服务器 IP，不从 Web 域名推断接口地址。
2. 明确 HTTP/HTTPS 策略。生产必须使用 HTTPS；如果开发环境继续使用 HTTP，需要单独提供 Android cleartext 白名单和有效期，不得默认放开所有域名。
3. 明确 Token 请求头格式。当前约定是 `Authorization: Token <token>`。
4. 提供登录、注册、退出、`profile` 和设置密码接口，以及 400/401/403/404/409/5xx 的错误结构和可展示文案。
5. 提供可用于隔离测试的临时账号。账号密码、Token 和服务器密钥通过安全渠道交接，不写入仓库、APK、日志或公开配置。
6. Android 端 Token 必须存入加密存储；退出、401 和切换账号时必须清理用户相关会话状态。

### 2.2 路由清单

Android 侧的路径常量位于：

`app/src/main/java/com/cscareerguide/app/data/bridge/CareerBridgeContract.kt`

| 功能 | 方法 | 路由 |
| --- | --- | --- |
| 登录 | POST | `/api/users/auth/login/` |
| 注册 | POST | `/api/users/auth/register/` |
| 退出 | POST | `/api/users/auth/logout/` |
| 个人资料读写 | GET/POST/PUT | `/api/users/profile/` |
| 设置密码 | POST | `/api/users/profile/set-password/` |
| 个人头像上传 | POST multipart | `/api/users/profile/`，字段 `avatar` |
| 简历读写 | GET/POST/PUT | `/api/resumes/my/` |
| 简历头像上传/删除 | POST/DELETE | `/api/resumes/upload_avatar/`、`/api/resumes/delete_avatar/` |
| 企业与职位 | GET | `/api/career-evaluation/computer-careers/` |
| 前辈经验 | GET/POST | `/api/ai/senior-advice/` |
| 目录岗位/企业/题库 | GET | `/api/career-platform/catalog/{kind}/` |
| 管理员目录导入 | POST | `/api/career-platform/catalog/imports/` |
| 简历分析 | GET/POST | `/api/career-platform/resume-analyses/` |
| 简历分析详情/应用 | GET/POST | `/api/career-platform/resume-analyses/{id}/`、`.../apply/` |
| 正式题库检索 | POST | `/api/career-platform/questions/retrieve/` |
| 题库检索演示 | POST | `/api/career-platform/questions/demo-retrieve/` |
| 练习会话列表/创建 | GET/POST | `/api/career-platform/practice-sessions/` |
| 练习详情/回答/结束 | GET/POST | `/api/career-platform/practice-sessions/{id}/`、`.../answers/`、`.../finish/` |
| 逐题反馈重试 | POST | `/api/career-platform/practice-sessions/{id}/answers/{answerId}/retry-feedback/` |
| 评估记录/创建/导出 | GET/POST | `/api/career-platform/evaluation-runs/`、`.../{id}/`、`.../{id}/export/` |
| 自由模拟面试 SSE | POST | `/api/ai/llm/chat/` |
| 面试历史 | GET | `/api/ai/llm/chat/history/` |
| 面试复盘列表/详情/保存 | GET/POST | `/api/ai/records/interview-reviews/`、`.../{sessionId}/`、`/api/ai/interview/save_review/` |
| ASR | POST multipart | `/api/asr/speech-to-text/`，字段 `audio`，16 kHz PCM |

接口是否已经部署、是否有数据，必须由后端负责人逐项确认；源码中存在路由不代表当前环境已经可用。

## 3. 各模块的桥接数据

### 3.1 目录

后端需提供：

- `count/page/page_size/results` 分页，或明确 Android 使用的等价分页结构。
- 岗位：ID、标题、企业、城市、薪资、经验、学历、技能、来源、采集时间、`permission_status`、`is_demo`。
- 企业：ID、名称、城市、行业、描述、来源、采集时间、`permission_status`、`is_demo`。
- 题库：ID、题干、岗位方向、知识点、难度、关键点、参考答案、来源、来源链接、采集时间、许可状态。
- 公开查询只返回 `allowed` 记录；`pending/restricted` 不能因客户端筛选而泄漏。
- 来源、采集时间和记录 ID 必须保留，不能只返回展示文案。

### 3.2 简历优化

后端需提供：

- 请求：`jd_text`、`resume_revision`、UUID `request_id`，可选 `job_id`。
- 分析记录：分析 ID、简历版本、建议 ID、章节、原文、引用证据、建议文本、当前分析状态。
- 应用请求：`base_revision`、非空 `accepted_suggestion_ids`、UUID `request_id`，以及可选的确认后文本。
- 版本冲突返回 409，并要求客户端刷新；不能覆盖较新的简历版本。
- 分析成功不会自动修改简历；只有明确应用且服务端返回新版本后才更新。
- 真实模型失败、超时和重试要保持可恢复，不伪造建议或成功结果。

### 3.3 题库检索和练习

后端需提供：

- 检索请求：`role`、`topic`、`difficulty`、`query`、`top_k`；返回题 ID、题干、关键点、相关度和逐字段来源证据。
- 练习会话创建、题目快照、回答、反馈、结束总结的完整状态机。
- 每个回答返回 `status`、`feedback`、`failure_code`、重试入口和原始题目证据。
- `request_id` 用于幂等创建/重试；网络重发复用同一请求 ID。
- 练习记录按当前 Token 隔离；不属于当前用户的记录返回 404。
- 记录不做本地“重新计算”或自动补入推荐题。

### 3.4 管理员评估

- 后端必须返回当前用户是否具备管理员能力；不能从 profile 字段猜测 `is_staff`。
- 需要冻结评估集版本、策略、Top-K、样本数、失败数、模型/策略版本和数据集指纹。
- 指标至少包括 Precision@K、Recall@K、F1@K、HitRate@K、MRR；逐例结果要可追溯到记录详情。
- 导出是同一记录的 JSON，不得在导出时重新运行策略。
- 403、404、409、评估任务失败和部分样本失败需要明确区分。

### 3.5 面试、语音和个人中心

- 自由面试使用 `POST /api/ai/llm/chat/` 的 SSE；Android 需要事件解析、取消、断线重连和错误状态。不能用一次普通 JSON 请求假装流式成功。
- ASR 上传单声道 16 kHz PCM，返回一次性 JSON；TTS 如继续使用，需要明确返回边界和播放生命周期。
- 面试复盘列表、详情、保存要按用户隔离，并返回题目数、复盘内容和时间。
- 个人资料、头像上传、简历摘要、职业推荐、复盘和练习记录均需稳定字段模型或明确缺失语义。
- 头像上传限制、MIME、大小和失败清理策略按后端文档执行，不能复用其他上传接口的限制。

## 4. Android 集成位置

当前可直接搜索 `TODO(bridge)`：

- `app/src/main/java/com/cscareerguide/app/MainActivity.kt`：替换 `InMemoryMockRepository`，注入真实 Repository/HTTP adapter。
- `app/src/main/java/com/cscareerguide/app/data/InMemoryMockRepository.kt`：把目录、分析、练习、评估和 profile 的 Mock 方法替换为实现同一契约的远程 Repository。
- `app/src/main/java/com/cscareerguide/app/data/bridge/CareerBridgeContract.kt`：补全 HTTP adapter、SSE、multipart 和结果映射；Screen 不直接调用。

桥接时应保持：

1. Screens 继续只接收 ViewModel state 和事件 callback。
2. ViewModel 不直接持有 Token、URL 或网络异常细节。
3. Repository 将 HTTP 错误映射为领域结果，UI 区分 loading/content/empty/error/retry。
4. 生产配置通过 BuildConfig/环境注入，不把 Token、密码或数据库凭据放进 `VITE_*` 等价公开变量。
5. 认证失败统一清理会话；退出后清除个人资料、推荐、简历、题目快照、练习和复盘界面状态。

## 5. 联调验收清单

- [ ] 后端提供开发/测试根地址、HTTPS 策略、Token 格式和稳定的 OpenAPI 或响应示例。
- [ ] 目录分页、权限过滤、来源和演示标记实际返回。
- [ ] 简历分析、版本冲突、选择性应用和重试实际返回。
- [ ] 题库检索、练习创建、回答、反馈、结束总结和记录详情实际返回。
- [ ] 管理员权限返回 403，不再依赖客户端猜测。
- [ ] 面试 SSE 事件、取消、超时和断线路径可观测。
- [ ] ASR PCM 上传、TTS 生命周期和失败提示可观测。
- [ ] 个人资料/头像、简历头像、密码和退出后的会话隔离验收。
- [ ] 临时账号、Token 在验收后清理；日志不包含 Token、密码、私钥或真实用户数据。
- [ ] 使用隔离测试环境，不因 Mock 或本地测试通过而自动切生产、迁移数据库或部署。

在以上项目完成前，当前 APK 仍只能作为 UI/Mock 验收产物。
