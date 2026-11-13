# App 端 API 接入文档

> 核对日期：2026-11-12。代码基线：本 fork `origin/master` 的 `208aae8`，本地 `codex/personal` 已快进到该提交。本文描述该提交实际挂载的后端接口；“已实现”仅表示存在业务实现，不表示当前服务器已部署、已有数据或真实模型已验收。随后按用户要求完成只读 SSH 检查和真实 curl 鉴权链路验证，临时账号已删除；未调用真实模型、未部署或修改服务器配置。**线上目录接口实测仍为501，与本地代码能力不同，联调先看下文线上状态表。**

本文面向 Android、iOS、Flutter、uni-app 等客户端开发者。App 复用 Django API、DRF Token 和现有流式协议，没有独立的 `/app/` 前缀。需求与分工仍以[当前需求方案](career_platform_plan.md)为准。

## 目录

- [1. 接入约定](#1-接入约定)
- [2. 接口能力与接入顺序](#2-接口能力与接入顺序)
- [3. 登录、注册与个人资料](#3-登录注册与个人资料)
- [4. 简历管理](#4-简历管理)
- [5. 岗位、企业和题库目录](#5-岗位企业和题库目录)
- [6. 正式题库练习与记录](#6-正式题库练习与记录)
- [7. 自由模拟面试与语音](#7-自由模拟面试与语音)
- [8. 职业路径、招聘查询与建议](#8-职业路径招聘查询与建议)
- [9. 职业推荐、规划及历史](#9-职业推荐规划及历史)
- [10. 简历 AI 优化预留接口](#10-简历-ai-优化预留接口)
- [11. 管理员接口](#11-管理员接口)
- [12. 错误、重试与联调检查](#12-错误重试与联调检查)
- [13. 代码定位与维护](#13-代码定位与维护)

## 1. 接入约定

### 1.1 地址、请求头与编码

由后端交付人员提供当前环境的可访问地址。本文不把开发地址、历史服务器地址写成生产入口。

**当前演示入口（2026-11-12 实测）**：`API_BASE_URL = http://123.207.252.243/api`。公网 `GET /api/users/profile/` 不带 Token 返回预期 JSON 401，响应含 `WWW-Authenticate: Token`。只读 SSH 确认 gateway、frontend、backend 容器均 healthy；gateway 映射宿主 80/48080 到容器8080。服务器本机访问48080同样返回预期401，但本次从开发电脑访问公网48080连接超时，App 联调优先用已确认可达的80端口。以上仅验证网关到后端及匿名鉴权，不覆盖登录后的业务、真实模型、当前运行镜像与代码基线的一致性或HTTPS；当前确认入口使用HTTP。

**线上带 Token 的 curl 实测（2026-11-12）**：使用单次临时虚构账号，依次执行下列请求；未调用模型或创建练习/回答，账号及其 Token 已清理。结果为本次部署快照，不代替以后重新验证。

| 实际请求 | HTTP | 本次结论 |
| --- | --- | --- |
| `POST /api/users/auth/register/` | 201 | 返回 user/token |
| `POST /api/users/auth/login/` | 200 | 返回 user/token |
| `GET /api/users/profile/`，有效 Token | 200 | 返回当前临时用户，ID 与注册响应一致 |
| `GET /api/career-platform/catalog/jobs/?page=1&page_size=1` | **501** | FEATURE_NOT_IMPLEMENTED；当前公网入口尚不可联调岗位目录成功态 |
| `GET /api/career-platform/catalog/questions/?page=1&page_size=1` | **501** | FEATURE_NOT_IMPLEMENTED；当前公网入口尚不可联调题库目录成功态 |
| `GET /api/career-platform/practice-sessions/?page=1&page_size=1` | 200 | count=0，包含 page/page_size/results |
| `POST /api/users/auth/logout/` | 200 | 空响应体 |
| `GET /api/users/profile/`，复用退出后的 Token | 401 | Token 撤销生效 |

上述目录501说明线上行为与本文代码基线不同；尚未核对具体运行镜像版本与差异原因，不能直接断言重启或发布即可修复。正式检索、创建练习、模型反馈、总结、简历读写和其他业务不在本次 curl 成功验收范围。脱敏本机证据为 `.runtime/app-api-curl-20261112-164928.json`（忽略目录，不包含密码或 Token）。

```text
API_BASE_URL = https://<后端网关域名>/api
完整请求 = API_BASE_URL + /users/profile/
```

所有接口路径均保留末尾 `/`。不要依赖重定向修正 POST 路径。原生 App 的 `localhost` 指设备自身，不能用来表示开发电脑。

除另行说明，写请求使用 UTF-8 JSON：

```http
Authorization: Token <登录或注册返回的token>
Content-Type: application/json
Accept: application/json
```

认证前缀为 `Token`。当前没有 JWT、refresh token、短信登录或 OAuth 接口。上传使用 `multipart/form-data`，文件字段见各接口，boundary 交给 HTTP 库生成。SSE 使用 `Accept: text/event-stream`。

App 只保存业务 Token；模型、ASR、TTS、数据库凭据留在后端。图片 URL 可能是绝对 URL，也可能以 `/media/` 开头；后者相对于网关源地址解析，不拼到 `/api/` 下。用户头像和简历头像是两份数据。

### 1.2 鉴权与身份

仅以下三个完整路径允许匿名：

| 方法 | 路径 |
| --- | --- |
| POST | `/api/users/auth/register/` |
| POST | `/api/users/auth/login/` |
| POST | `/api/users/auth/reset-password/` |

其他 `/api/` 入口，包括健康检查、演示检索，都需要有效 Token。普通 OPTIONS 返回 204，不代表业务请求成功。登录、注册、重置密码请求省略旧的 Authorization 头。

登录/注册通过 `get_or_create` 返回 Token，同一用户再次登录可能取得同一 Token；没有客户端刷新接口，也未配置本协议层的定时过期。退出和匿名重置密码会删除当前用户 Token，使使用该 Token 的其他设备同时失效。收到 401 后清除登录态并重新登录；网络失败、500、503 不等于 Token 失效。

服务端从 Token 确定用户，不接收客户端指定 `user_id` 来切换身份。`career-platform` 记录接口对不存在或不属于当前用户的对象返回 404。管理员接口要求 `is_staff=true`；当前用户响应不包含 `is_staff`，客户端不能从现有 profile 字段推断管理员身份。

### 1.3 返回结构、分页与类型

历史模块没有统一成功包装，按本文各接口解析：

| 类型 | 返回结构 | 典型接口 |
| --- | --- | --- |
| 对象直返 | `{id, ...}` | profile、简历、正式练习详情 |
| 新课程分页 | `{count, page, page_size, results}` | catalog、practice-sessions、evaluation-runs |
| DRF 分页 | `{count, next, previous, results}` | 职业路径、计算机岗位、企业、招聘会 |
| 历史 AI 包装 | `{success, data/...}` | 旧复盘、推荐、历史 |
| 流式文本 | `data: ...` 帧 | `/api/ai/llm/chat/` |

新课程查询参数为 `page>=1`（默认 1）、`page_size=1..100`（默认 20）。传统列表默认每页 10；企业/企业职位信息/招聘会为 15；学长建议为 10。只有明确配置 `page_size` 参数的接口才支持调整大小，不能把新课程分页规则套到所有列表。DRF 越界页通常为 404，新课程分页可返回空 `results`。

用户、简历、企业、岗位通常使用整数 ID；正式题目、练习会话、回答和评估使用 UUID 字符串。旧 AI `session_id` 是不透明字符串，与正式练习 UUID 不互通。金额可能是字符串或 null，保留精度。时间按返回偏移量解析；旧复盘的 `created_at` 为 `YYYY-MM-DD HH:mm`，无偏移量，不当作 UTC 强制解析。

`/api/career-platform/` 使用严格入参校验，未声明字段返回 400；不要附带 `user_id`、客户端题目快照等额外内容。本文示例中的 ID、文字和结果均为虚构说明，不是已导入数据。实际接入必须使用查询响应中的 ID。

## 2. 接口能力与接入顺序

| 功能 | 当前代码状态 | App 端接入说明 |
| --- | --- | --- |
| 账号、资料、简历 CRUD | 已实现 | 可先接入基本表单和用户隔离 |
| 正式企业/岗位/题库目录 | 已实现 | 走 `career-platform/catalog`；数据取决于环境导入 |
| 正式题库检索 | 已实现 | 当前为数据库题库上的文本检索，无需模型；不宣称已接向量检索 |
| 逐题回答、反馈、结束总结、历史 | 已实现 | 反馈/总结依赖后端模型；失败后按状态恢复 |
| 独立演示检索 | 已实现，只读虚构材料 | 与正式练习区分；结果不能用来创建正式练习 |
| 自由模拟面试 | 已实现 legacy 主链路 | `POST /api/ai/llm/chat/` + SSE；客户端组织提示词 |
| ASR/TTS | 已有实现，依赖后端配置 | ASR 上传 PCM；TTS 随聊天 SSE 返回，不存在独立 TTS 路由 |
| 职业路径、招聘列表、建议 | 已实现 | 不同数据源/字段模型，分别接入 |
| AI 职业推荐/规划 | 已有实现，依赖模型/检索配置 | 存在回退和历史返回差异，见第 9 节 |
| 简历 AI 分析/确认应用 | 业务未实现 | 有路由和校验，有效业务调用返回 501；不能以此交付优化闭环 |
| 评估运行 | 已实现，管理员专用 | 当前内置 question 数据集与离线文本策略，见第 11 节 |
| 独立 `/company-api` 服务 | 非 Django API，默认网关未提供 | 网页中的独立企业客户端不能视为可用 App 后端 |

建议依次联调：注册/登录 → profile → 简历 → 目录 → 正式检索 → 创建练习 → 提交回答 → 结束/查看记录。自由面试作为另一条入口单独联调。

## 3. 登录、注册与个人资料

### 3.1 注册

`POST /api/users/auth/register/`，匿名，成功 **201**。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | string | 是 | 唯一用户名，模型最大 150 字符 |
| password | string | 是 | 当前注册序列化器未声明与设置密码相同的 6–128 限制 |
| confirm_password | string | 是 | 与 password 相同 |
| email | string | 否 | 可省略或空字符串，非空时须为邮箱 |

```json
{"username":"app_demo_user","password":"ExamplePass123","confirm_password":"ExamplePass123","email":""}
```

成功：`{"user": <User>, "token": "<token>"}`。重复用户名、密码确认不一致等返回 400 字段错误。

### 3.2 登录、退出与密码

| 方法与路径 | 请求 JSON | 成功响应 | 行为 |
| --- | --- | --- | --- |
| `POST /api/users/auth/login/` | `username`, `password` | 200，`{user, token}` | 错误凭据返回 400，不是 401 |
| `POST /api/users/auth/logout/` | 无，可不传 body | 200，空响应体 | 必须带 Token；不要无条件解析 JSON |
| `POST /api/users/profile/set-password/` | `new_password` | 200，`{message}` | 当前用户直接设置；无需旧密码，保持 Token |
| `POST /api/users/profile/change-password/` | `old_password`, `new_password`, `confirm_password` | 200，`{message}` | 校验旧密码和确认；保持 Token；新密码至少 6 字符 |
| `POST /api/users/auth/reset-password/` | `username`, `new_password` | 200，`{message}` | 匿名演示入口；成功撤销该用户 Token，不自动登录 |

`set-password`、`reset-password` 的新密码为 6–128 字符，保留首尾空格。未知或停用用户名重置返回 400。

**重置入口的实际边界：仅凭用户名即可改密码，没有短信、邮箱或验证码验证，是项目明确保留的隔离演示便利功能。App 若展示它，必须按现有演示说明标明风险，不能包装为已验证身份的正式找回流程。** 详见[统一登录门槛](authentication-gate.md)。

### 3.3 用户对象与资料更新

`GET /api/users/profile/` 返回当前 `<User>`；`PUT /api/users/profile/` 更新，实际采用 `partial=True`，只传需要改变的字段，成功 200 返回 `<User>`。未挂载 PATCH。

`<User>` 返回字段：

| 字段 | 类型/说明 |
| --- | --- |
| id、username、email | 整数 ID、用户名、邮箱 |
| avatar | 图片 URL；没有头像时为默认头像地址 |
| name、gender | 姓名；gender 为 `男`、`女` 或 null，更新空字符串会归一为 null |
| school、major、grade、education | 学校、专业、年级、学历 |
| github、profile、address | GitHub URL、个人简介、地址 |
| date_joined、last_login | 注册时间、最近登录时间，后者可能为 null |

可更新 `email, avatar, name, gender, school, major, grade, education, github, profile, address, skills, other_skills, self_introduction, career_goal`。其中最后四个文本字段目前**允许写入但不出现在 UserSerializer 的读取响应**；App 不应假设刷新 profile 后能恢复这些值。姓名/学校/专业最大 100，年级/学历最大 50，地址最大 200；GitHub 为合法 URL。

```json
{"name":"示例同学","gender":null,"school":"示例大学","major":"计算机科学","skills":"Java,SQL","career_goal":"后端开发"}
```

上传用户头像：`POST /api/users/profile/`，multipart 文件字段 `avatar`，成功 200 返回 `<User>`；无文件或 MIME 不以 `image/` 开头返回 400。本入口没有声明与简历头像相同的 5 MiB 限制，不混用两者校验规则。

## 4. 简历管理

### 4.1 推荐使用的接口

| 方法与路径 | 用途 | 响应 |
| --- | --- | --- |
| `GET /api/resumes/my/` | 当前用户简历 | 200 `<Resume>`；没有简历为 404 |
| `POST /api/resumes/my/` | 创建或部分保存当前简历 | 200 `<Resume>`，首次创建也是 200 |
| `PUT /api/resumes/my/` | 同上 | 已存在时部分更新 |
| `POST /api/resumes/upload_avatar/` | 上传简历头像，multipart `avatar` | 200 `{message, avatar_url}` |
| `DELETE /api/resumes/delete_avatar/` | 删除简历头像 | 200 `{message}`；没有头像 400，没有简历 404 |

简历头像支持 `image/jpeg, image/png, image/gif, image/webp`，最大 `5*1024*1024` 字节。上传入口在简历不存在时会先创建空简历，包括随后文件校验失败的情况。

### 4.2 简历对象与保存语义

`<Resume>` 字段：`id, edit_revision, avatar, avatar_url, name, target_position, phone, email, address, self_evaluation, skills, education, experience, projects, certificates, awards, created_at, updated_at`。

基本文本可为空；`skills` 是文本，不是字符串数组。姓名/意向岗位最大 100，手机最大 20，地址最大 200，邮箱按邮箱校验。`id/created_at/updated_at/avatar_url` 为只读，`edit_revision` 来自不可编辑模型字段，客户端读取即可。

| 嵌套数组 | 子项字段 | 非空必填字段 |
| --- | --- | --- |
| education | `id, school, major, period, description` | school、period |
| experience | `id, company, position, period, description` | company、position、period |
| projects | `id, name, role, period, url, description` | name、role、period |
| certificates | `id, name, issuer, period, description` | name、issuer、period |
| awards | `id, name, issuer, period, description` | name、issuer、period |

```json
{
  "name":"示例同学",
  "target_position":"Java后端开发",
  "skills":"Java、Spring、SQL",
  "education":[{"school":"示例大学","major":"计算机科学","period":"2023–2027","description":"本科在读"}],
  "experience":[],
  "projects":[{"name":"课程练习平台","role":"后端开发","period":"2026","description":"实现登录与练习记录"}]
}
```

**数组采用整体替换**：省略数组则保留；传 `[]` 则清空；传非空数组会删掉该类型旧子项并重建，子项 ID 可能变化，不是按 ID 合并。当前普通简历保存不接受版本冲突保护参数，更新会递增 `edit_revision`，不要把它误当作已具备乐观锁。建议保存成功后用完整响应更新本地状态。

DRF 还挂载了 `/api/resumes/` 的 GET/POST 和 `/api/resumes/{id}/` 的 GET/PUT/PATCH/DELETE，均按当前用户过滤；详情删除成功 204。根 GET 被重写为当前简历对象或 404，**不是分页列表**；根 POST 为创建、成功 201，已有单份简历时优先使用 `my/`，避免唯一约束冲突。

## 5. 岗位、企业和题库目录

正式课程数据入口如下，全部需要 Token、成功 200：

| GET 路径 | 支持的筛选 | 搜索含义 |
| --- | --- | --- |
| `/api/career-platform/catalog/companies/` | `city, q, page, page_size` | q 搜企业名、行业、介绍 |
| `/api/career-platform/catalog/jobs/` | `city, role, q, page, page_size` | role 搜标题/JD；q 也搜公司名 |
| `/api/career-platform/catalog/questions/` | `role, topic, difficulty, q, page, page_size` | role/topic/difficulty 为精确条件；q 搜题干/答案等 |

q 最大 500，city/role/topic 最大 100；difficulty 为 `easy|medium|hard`。企业不能附带 role/topic/difficulty，岗位不能附带 topic/difficulty，题目不能附带 city，否则 400。省略无筛选条件，不传空字符串占位。仅返回 `permission_status=allowed`；岗位另外排除已过 `valid_until` 的记录。空库正常返回 count=0，不自动导入样例。

```http
GET /api/career-platform/catalog/questions/?role=Java%E5%90%8E%E7%AB%AF&difficulty=easy&page=1&page_size=20
```

返回通用分页：

```json
{"count":0,"page":1,"page_size":20,"results":[]}
```

三种行对象字段：

| 对象 | 返回字段 |
| --- | --- |
| 企业 | `id, name, city, scale, nature, ranking, avg_salary, industry, website, public_profile, description, warning_info, source, source_url, permission_status, collected_at, verified_at, is_demo, dedupe_key, created_at, updated_at` |
| 岗位 | `id, company_id, company:{id,name,city}, title, salary_min, salary_max, city, experience, education, description, requirements, skills[], source, source_url, permission_status, collected_at, valid_until, is_demo, dedupe_key, created_at` |
| 题目 | `id(UUID), category, role, topic, difficulty, question, key_points[], reference_answer, source, source_url, permission_status, collected_at, tags[], is_demo, dedupe_key, created_at, updated_at` |

目录题目查询目前包含 `reference_answer`，客户端若做练习应自行按交互控制展示时机；正式检索和练习快照不返回该独立字段。`is_demo=true` 的数据必须展示演示标识。不要将 `/career-evaluation/computer-careers/` 的整数 ID 当作本目录 `job_id`，它们属于不同表。

## 6. 正式题库练习与记录

以下接口都在 `/api/career-platform/` 下，成功均为 **200**，包括创建会话、回答和结束；不要只按 201 判断创建成功。检索不调用生成模型，回答反馈与结束总结为同步 JSON 请求，没有 SSE。

### 6.1 检索题目

`POST /api/career-platform/questions/retrieve/`

| 字段 | 类型 | 必填/默认 | 约束 |
| --- | --- | --- | --- |
| role | string | 必填 | 最大 100，填写目录里的岗位方向 |
| topic | string | 可省略/空 | 最大 100 |
| difficulty | string | 默认 easy | easy/medium/hard |
| job_id | integer | 可选 | 正式目录岗位正整数 ID |
| jd_text | string | 可选/空 | 最大 20000，与 job_id 不可同时非空 |
| top_k | integer | 默认 5 | 1–20 |
| request_id | UUID string | 必填 | 客户端生成；本检索当前每次产生新 retrieval_id，不保证同键回放 |

```json
{"role":"Java后端","topic":"集合","difficulty":"easy","jd_text":"Java List Set 集合","top_k":5,"request_id":"10000000-0000-4000-8000-000000000001"}
```

```json
{
  "count":1,
  "retrieval_id":"20000000-0000-4000-8000-000000000001",
  "message":"",
  "results":[{
    "question_id":"30000000-0000-4000-8000-000000000001",
    "question":"List 与 Set 的区别是什么？",
    "key_points":["List 允许重复元素","Set 不保存重复元素"],
    "evidence":[{"source_id":"30000000-0000-4000-8000-000000000001","field_path":"source","quote":"团队自编示例"}],
    "score":0.6,"origin":"catalog","role":"Java后端","topic":"集合","difficulty":"easy",
    "permission_status":"allowed","collected_at":null,"source":"团队自编示例","source_url":""
  }]
}
```

score 为文本排序分，不是面试成绩。无命中仍为 200，`count=0/results=[]`，message 提示调整条件。当前检索结果的 `collected_at` 固定 null，详细来源时间可查目录，不能据此伪造采集时间。

返回 `retrieval_id` 为绑定当前用户的缓存回执，默认有效 900 秒，可由后端 `PRACTICE_RETRIEVAL_TTL` 调整。缓存丢失/过期后创建练习可能 409；重新检索后使用新回执和新的创建 request_id。不要把 receipt 当作可长期收藏记录。

### 6.2 创建会话

`POST /api/career-platform/practice-sessions/`

字段包括 `role, topic, difficulty, job_id, jd_text, request_id, question_ids, retrieval_id`；公共文本约束与上节一致。`question_ids` 虽在输入序列化器中标为可选，**实际创建至少需要 1 道、至多 20 道，且不能重复**。request_id 为新的创建操作 UUID。

```json
{
  "role":"Java后端","topic":"集合","difficulty":"easy",
  "question_ids":["30000000-0000-4000-8000-000000000001"],
  "retrieval_id":"20000000-0000-4000-8000-000000000001",
  "request_id":"10000000-0000-4000-8000-000000000002"
}
```

有回执时，role/topic/difficulty 必须与检索一致，question_ids 必须属于该回执。不带回执也可从正式目录选题，后端重新检查题目存在、allowed、角色/知识点/难度匹配。客户端不上传题目正文、参考答案或快照。岗位和简历快照由后端生成。

同一用户、同一创建 request_id、相同输入返回原会话；同键不同输入返回 409。成功返回完整 `<PracticeSession>`：

| 字段 | 类型/含义 |
| --- | --- |
| id、status | UUID；`active` 或 `ended` |
| created_at、ended_at | ISO 时间；结束前 ended_at 为 null |
| role、topic、difficulty | 本次筛选条件 |
| filters | 筛选/选题信息，不包含内部 request_payload |
| job_snapshot | 岗位或粘贴 JD 的快照；含 id/title/company/description/requirements/skills/city/source/source_url/permission_status/collected_at/is_demo/jd_source |
| resume_snapshot | 创建时简历摘要与经历快照；无简历时 `{}`；只展示用户可见内容 |
| presented_questions | 后端确认的题目快照数组，按所选顺序 |
| answers | `<AnswerRecord>[]`，初始为空 |
| summary | `<Summary>`，初始 pending |

题目快照字段包括 `question_id, question, key_points, evidence, source, source_url, permission_status, collected_at, role, topic, difficulty, origin, score`，部分还包含 `retrieval_context`。后续回答只用此处下发的 question_id。

### 6.3 提交回答及反馈失败恢复

`POST /api/career-platform/practice-sessions/{session_id}/answers/`

```json
{"question_id":"30000000-0000-4000-8000-000000000001","answer":"List 可以包含重复元素，Set 用于去重。","request_id":"10000000-0000-4000-8000-000000000003"}
```

answer 为非空文本，最大 20000；question_id 和 request_id 为 UUID。返回 `<AnswerRecord>`：

| 字段 | 含义 |
| --- | --- |
| id | 回答 UUID，反馈重试使用它 |
| question_id、question_snapshot | 原题 ID 与会话内的题目快照 |
| answer | 用户实际回答 |
| status | `received`（等待/生成中）、`succeeded`、`failed` |
| failure_code | 失败类型；成功时空字符串 |
| feedback | 成功的反馈对象；未成功通常 `{}` |
| created_at、updated_at | ISO 时间 |

feedback 字段：`answer_quote` 为实际回答中的原文片段，`key_points_covered[]` 为覆盖点，`missing_points[]` 为待补点，`evidence[]` 为题目来源证据，`follow_up` 为字符串或 null。客户端容忍数组为空，不从空反馈自行生成分数。

同一回答 request_id 的相同输入只回放，不重复调用模型；同键不同输入为 409。同一道题换新 request_id 再提交也会 409，当前未实现“再答一次”。已结束会话不接受新回答。

**反馈接口先保存回答，再调用模型。** 模型输出无效返回 502；不可用 503；超时 504。这些错误不表示回答未保存，错误体也不保证带回答 ID。App 保留原文及原 request_id，先 GET 会话详情，在 answers 中按 question_id 找到回答和 status，随后恢复：

- 已 succeeded：展示已有结果。
- received：显示处理中，间隔查询详情；不要密集创建新请求。
- failed：展示“回答已保存，反馈失败”，允许用户明确重试反馈。
- 详情无回答且提交结果不确定：以原 request_id 和相同内容重发原回答。

重试反馈：`POST /api/career-platform/practice-sessions/{session_id}/answers/{answer_id}/retry-feedback/`，body 为 `{"request_id":"<本次重试的新UUID>"}`，成功返回同一个 `<AnswerRecord>`。网络重发复用该重试 UUID；用户发起下一次新的模型尝试才换 UUID。成功反馈不再生成；同一重试键不重复生成；received 未超时会直接返回当前状态。默认最多 5 次尝试（含首次），达到上限 409；超时接管阈值默认是 `LLM_TIMEOUT + 30秒`，以服务端配置为准。会话结束后仍可按该入口恢复已有回答反馈，不创建新回答。

### 6.4 结束与总结重试

`POST /api/career-platform/practice-sessions/{session_id}/finish/`

```json
{"request_id":"10000000-0000-4000-8000-000000000004","retry_summary":false}
```

首次结束即写入 `status=ended` 和 ended_at，再生成总结。成功 HTTP 200 返回完整 `<PracticeSession>`。**即使模型生成失败也会返回 200**，客户端必须看 summary.status，不能只看 HTTP 状态。

```json
{"status":"failed","strengths":[],"gaps":[],"next_steps":[],"failure_code":"MODEL_TIMEOUT","prompt_version":"v1"}
```

`<Summary>` 包含 `status:pending|succeeded|failed`、`strengths:string[]`、`gaps:string[]`、`next_steps:string[]`、`failure_code`、`prompt_version`，没有数值评分。无回答结束时为 `failed/NO_ANSWERS`，不调用模型且不能靠重试生成总结。

普通重复 finish 只返回当前记录。明确重试失败/过期总结：同一路径提交 `retry_summary:true` 和新 request_id；网络重发复用该键。成功总结不重复生成；处理中未达到超时返回 pending；默认总尝试上限同样为 5，超限 409。原回答和首次 ended_at 保留。结束和总结是两个状态，summary 失败不重新开放作答。

### 6.5 历史列表与详情

| 方法与路径 | 参数 | 响应 |
| --- | --- | --- |
| `GET /api/career-platform/practice-sessions/` | `page,page_size,q,role,topic,difficulty` | 新课程分页；按创建时间倒序 |
| `GET /api/career-platform/practice-sessions/{session_id}/` | 无业务必填查询参数 | 完整 `<PracticeSession>`；无权/不存在 404 |

列表每项为 `id, status, created_at, ended_at, role, topic, difficulty, job_title, answer_count, summary_status`。q 搜岗位标题/角色；详情才含题目、回答和完整总结。当前没有正式练习删除/修改/导出接口；不要调用旧 AI records/delete 处理练习 UUID。

### 6.6 只读演示检索

`POST /api/career-platform/questions/demo-retrieve/`：`{query,role,topic,difficulty,top_k}`，role 必填，query 默认空（最大 500），topic 默认空，difficulty 默认 easy，top_k 默认 5（1–20），**不接受 request_id/jd_text/job_id**。

返回 `{is_demo:true,dataset:"text-retrieval-demo-v1",count,results,message}`，每项含 `question_id,question,key_points,evidence,score,origin,is_demo:true`。此接口使用独立虚构材料，不写数据库、不创建会话、不返回 retrieval_id；不把结果送入正式创建会话。

## 7. 自由模拟面试与语音

### 7.1 共享聊天接口

`POST /api/ai/llm/chat/`，使用 JSON 请求和 SSE 响应。它是当前网页 legacy 自由面试主链路；与第 6 节题库练习相互独立。

| 字段 | 类型/默认 | 说明 |
| --- | --- | --- |
| message | 必填 string | 用户文本或客户端拼接的开始/作答/复盘提示词 |
| mode | string | `normal`、`career`/`career_consultant`、`interview`、`interview_review` |
| interview_style | string | `gentle`、`pressure`、`technical`、`behavioral` |
| session_id | string，可省略/空 | 首次为空由后端生成；后续使用 end 事件返回值 |
| enable_tts | boolean，默认 false | JSON 布尔 true 才申请语音；服务端也须 ENABLE_TTS=true，且为面试模式 |

```json
{"message":"【面试模式】请以温和友好的风格开始一场10分钟的Java后端面试，先自我介绍并提出第一个问题。","mode":"interview","interview_style":"gentle","session_id":"","enable_tts":false}
```

后端也通过 `【面试模式】`、`【面试复盘模式】` 等文本标记识别模式。网页压力面试历史请求使用 `mode=pressure_interview`，靠 message 标记进入面试；App 直接使用 `interview` 与 style=pressure 更清楚。不要把 LangGraph 相关文件的存在理解为已有独立 App 面试状态机。

面试开始、追问、计时、简历文本和复盘对话由客户端组织。后端不提供 `durationMinutes` 字段或自动结束接口，也不会替 App 读取设备上的简历。可先查 `/resumes/my/`，再将需要的摘要加入 message。完整网页兼容提示词见 `frontend/src/features/interview/legacy/implementation.js`。

### 7.2 SSE 帧与完成顺序

```text
data: {"type":"chunk","content":"你好，"}

data: {"type":"chunk","content":"请介绍你的项目。"}

data: {"type":"audio","audio":"<Base64编码的MP3片段>"}

data: [DONE]

data: {"type":"end","session_id":"<本次会话字符串>"}

```

| 帧 | App 处理 |
| --- | --- |
| chunk | 按顺序追加 content，不能覆盖前面的文本 |
| audio | Base64 解码为 MP3 音频块，按顺序缓冲播放；后端配置采样率 22050 Hz |
| error | content 为错误描述，保留已收到文字并展示失败 |
| `[DONE]` | 非 JSON 标记；继续读取后续 end 事件 |
| end | 保存 session_id；流仍以连接结束作为读取结束 |

**当前代码先发 `[DONE]`，后保存对话，再发 end。收到 `[DONE]` 就关闭连接会丢 session_id。** App 使用支持 POST、认证头和字节流的 HTTP 客户端；不要假定标准 EventSource GET 即可调用。按 UTF-8 增量解码，按空行分帧，处理一个网络块含多帧、单帧跨块、中文跨字节块及 CRLF。参考网页 `legacy/stream.js`。

HTTP 非 2xx 在进入流解析前按 JSON/文本错误处理；HTTP 200、收到 `[DONE]` 或连接 EOF 都不能单独证明生成完整或历史保存成功。当前 SSE 不保证所有工作线程模型异常都转成 error 帧；收到 end、业务文本及后续查询结果分别处理，异常断流显示可恢复失败。客户端取消接收也不保证后端立即取消模型调用。

### 7.3 生成和保存复盘

先调用相同聊天接口，`mode=interview_review`、`enable_tts=false`，message 带复盘要求与完整面试对话。收集 chunk 后单独保存：

`POST /api/ai/interview/save_review/`

```json
{"session_id":"<end返回的原会话ID>","review_content":"<完整复盘文本>","interview_style":"gentle","interview_duration":10,"question_count":5,"answer_count":5}
```

session_id 必填；review_content 默认为空，style 默认 gentle，时长单位为**分钟**，计数默认 0。客户端应提供实际文本与计数。成功 200：`{success:true,message,review_id}`。此接口每次创建复盘记录，没有 request_id 幂等；自动重发可能重复保存。

| GET 路径 | 返回 |
| --- | --- |
| `/api/ai/records/interview-reviews/` | `{success:true,data:[...]}`，最新 10 条，无分页 |
| `/api/ai/records/interview-reviews/{session_id}/` | `{success:true,data:{...}}`，按当前用户复盘查询，不存在 404 |

列表项：`id, session_id, interview_style, overall_score, interview_duration, pair_count, created_at, latest_content`；pair_count 对应 question_count。详情字段：`id,session_id,interview_style,overall_score,interview_duration,question_count,answer_count,review_content,strengths,weaknesses,suggestions,created_at`。旧 overall_score 来自 legacy 复盘解析，不与正式练习总结混用。会话不存在时视图仍会尝试用空 conversation 保存，而当前模型外键不允许空值，可能返回500；应先取得有效聊天会话 ID。

### 7.4 语音识别

`POST /api/asr/speech-to-text/`，multipart 文件字段 `audio`，内容为 **16 kHz PCM**。当前后端将上传 bytes 直接送 ASR，不做 MP3/M4A/WebM/WAV 容器自动解码；App 参照网页录音处理输出单声道 16 位 PCM。响应为一次性 JSON，不是实时 WebSocket：

```json
{"result":"success","text":"识别出的回答"}
```

无音频为 400 `{result:"音频不存在"}`；服务失败 502；超时 504；错误说明放 result，不是统一 error 字段。服务端 ASR 超时上限 120 秒。成功后再把 text 送聊天/正式回答接口，上传到 chat 的 audio 本身不会替代独立识别流程。

TTS 无独立 HTTP 入口，由聊天请求显式 enable_tts 和服务端配置共同决定；没有 audio 帧时仍正常展示文字。原生录音权限、WebView 的 HTTPS/麦克风条件和音频播放要在对应设备验证。

## 8. 职业路径、招聘查询与建议

### 8.1 职业路径与问卷

| 方法与路径 | 参数/响应 |
| --- | --- |
| `GET /api/career-paths/paths/` | 默认 DRF 分页，page；项含 `id,path_type,title,description,preparation_advice,time_plan,resource_links,created_at,updated_at` |
| `GET /api/career-paths/paths/{id}/` | 单个路径对象 |
| `GET /api/career-paths/assessments/` | 默认 DRF 分页，page；只列 is_active=true；项含 `id,question,options,path_weights,order,is_active` |
| `GET /api/career-paths/assessments/{id}/` | 单个问卷题目 |
| `POST /api/career-paths/assessments/submit/` | JSON `{"answers":{"<题目整数ID>":"<选项值>"}}`，成功 201 |

提交返回 `{session_id,recommended_path,confidence_score,path_scores}`。方向枚举为 `employment,postgraduate,civil_service,military,entrepreneurship`；confidence_score 是权重比值，不是招聘成功率。客户端从 options/path_weights 取得实际选项值；加载所有分页题目再提交。当前后端对缺答/无效选项未做完整性拒绝，空答案也可能返回默认最高项，App 不把它展示为有效完成测评。没有挂载该结果的列表/详情路由。

### 8.2 计算机岗位查询

`GET /api/career-evaluation/computer-careers/`，筛选 `position_name,company_name,address,industry,search,page,page_size`，前四个及 search 为包含匹配，page_size 默认 10、最大 100，按主键稳定排序。返回 DRF 分页。

项字段：`id,position_name,address,salary_range,company_name,industry,company_size,company_type,position_code,position_details,update_date,company_details,position_source_url`。`GET /api/career-evaluation/computer-careers/{id}/` 返回单项。数据读取失败 503：`{code:"JOBS_UNAVAILABLE",message}`。

`GET /api/career-evaluation/categories/` 及 `/{id}/` 返回职业分类；列表默认 DRF 分页，每项 `id,name,market_demand,description,subcategories[]`，子项 `id,name,market_demand,skills,description`。这些是职业分类，不是正式岗位 JD。该 ViewSet 代码还挂载标准 POST/PUT/PATCH/DELETE，但没有专门的 staff 写保护，App 普通展示流程只接 GET；这是现有权限边界待完善项，不据此设计用户编辑公共分类的功能。

### 8.3 企业与企业职位信息

| GET 路径 | 筛选/返回 |
| --- | --- |
| `/api/company-reviews/companies/` | `job_title,city,scale,nature,min_salary,max_salary,ranking,page,page_size`；默认 15/最大100；DRF 分页 |
| `/api/company-reviews/companies/{id}/` | 企业详情 |
| `/api/company-reviews/companies/filter_options/` | `{cities:[],scales:{},natures:{},rankings:{}}` |
| `/api/company-reviews/company-info/` | `job_title,company_name,address,industry,company_scale,company_type,page,page_size`；DRF 分页，默认15/最大100 |
| `/api/company-reviews/company-info/{id}/` | 企业职位单条详情 |
| `/api/company-reviews/company-info/filter_options/` | `{cities:[],scales:[],types:[],industries:[]}` |
| `/api/company-reviews/company-info/companies/` | 按企业分组的数组，见下文；不使用 DRF 包装 |

旧 companies 对象：`id,name,city,scale,nature,ranking,avg_salary,description,warning_info,jobs[]`，jobs 子项 `id,title,salary_min,salary_max,requirements`。job_title 同时搜索企业名和职位标题；city/scale/nature/ranking 为精确条件；薪资过滤比较的是企业 avg_salary。枚举以 filter_options 响应为准。

company-info 对象采用另一套字段：`id,title,company,salary,tags[],jobDetails,companyDetails,jobSourceUrl`，注意驼峰字段。companies 分组接口接收 `job_title,address,industry,company_scale,company_type,page,page_size`，默认 page=1/page_size=15，返回裸数组：每项 `name,scale,type,industry,description,job_count,jobs[]`，jobs 最多 5 项，字段 `job_title,salary_range,address,job_source_url`；没有 count/next。

上述旧列表不等同于第 5 节经过 allowed 过滤的目录，也没有统一的时效审核保证。网页 `companyAPI` 的 `/company-api/search`、`/city/...`、`/job/...`、`/ai-search`、`/companies/stats` 属于独立服务，Django 未挂载，默认生产网关返回 503，App 不依赖这些路径。

### 8.4 高校双选会

`GET /api/company-reviews/job-fairs/`：`university,page,page_size`，默认15/最大100，university 精确匹配，按 date 倒序，返回 DRF 分页。详情为 `GET /api/company-reviews/job-fairs/{id}/`。字段 `id,university,name,date,location,contact_phone,contact_email,description,registration_link,university_url,created_at,updated_at`。

`GET /api/company-reviews/job-fairs/filter_options/` 返回 `{universities:{code:displayName}}`。数据库读取失败为 503 `{code:"JOB_FAIRS_UNAVAILABLE",message}`。当前 ViewSet 还挂载标准 POST/PUT/PATCH/DELETE，只有登录权限，没有单独 staff 写限制；普通 App 只接查询，写权限问题与业务实现分开记录。

### 8.5 学长学姐建议

`GET /api/ai/senior-advice/`：`search` 搜 company/position；`ordering=recent|rating`（默认 recent）；`page,page_size` 默认10/最大50。只返回已审核建议，格式 `{success:true,count,next,previous,results}`。

`POST /api/ai/senior-advice/`：必填 `company,position,senior_name,graduation_year,advice,rating`；可选 `current_company,salary_info,pros,cons`。毕业年份为四位字符串，rating 为 1–5 的有限数，pros/cons 为字符串数组，子项最大500。current_company 最大255，salary_info 最大100。

成功 **201**：`{success:true,message,data:<Advice>}`，新记录 is_approved=false，投稿后不会立即出现在公开列表。Advice 字段为 `id,company,position,senior_name,graduation_year,current_company,advice,salary_info,pros,cons,rating,created_at,is_approved`。校验失败400 `{success:false,message,errors}`；数据库异常503 `ADVICE_UNAVAILABLE`。

## 9. 职业推荐、规划及历史

### 9.1 生成类接口

| POST 路径 | 请求 | 成功响应 |
| --- | --- | --- |
| `/api/ai/llm/career/recommendation/` | `self_introduction` 非空文本 | `{success,recommendations[],raw_analysis,session_id,source,match_score,response_time,timing}`，可能另带 cards/accordion |
| `/api/ai/llm/career/plan/` | `self_introduction` 文本、`selected_careers` 非空数组 | `{success:true,data:<模型解析结果>}`，解析失败500 |
| `/api/career-report/` | `target_job` 非空文本、可选 `user_profile` 对象 | `{success:true,session_id,target_job,data:<结构化报告>}`，自动保存报告 |
| `/api/ai/career-report/` | 同上一接口 | 同一视图的别名，App 固定用一个入口 |
| `/api/ai/voice-to-resume/` | `voice_text` 非空文本 | `{success:true,data:<生成的简历草稿>,message}`，不自动保存简历 |

推荐项通常为 `{career,matchScore,reason}`；卡片/折叠结构为历史兼容扩展，缺失时退回 recommendations 和 raw_analysis。source 可能为 `llm,cache,llm_fallback,default`。当前实现存在默认推荐、模型异常/非 JSON 回退，HTTP 200 / success=true 不能证明真实模型生成成功或质量合格；展示时保留来源。缓存分支 session_id 可能为 `cached` 或空，不能据此保证可读取用户历史。

职业报告 user_profile 可包含 `name,major,education,skills[],personality,graduation_year,school,grade,career_goal,self_introduction`；省略或空对象时从当前用户资料构造。data 的内容来自报告工作流，客户端按实际结构渲染并容忍部分生成失败；本接口没有另行冻结完整 App DTO。

语音简历输入的是**识别后的文本**，不是音频文件。生成结果先预览和编辑，再通过 `/api/resumes/my/` 保存；它与仍未实现的正式 JD 简历分析不同。

### 9.2 历史保存接口

以下均为 POST、成功 200，session_id 必填，不具备 request_id 幂等，自动重试可能重复写入：

| 路径 | 其他请求字段及默认 | 成功响应 |
| --- | --- | --- |
| `/api/ai/llm/save-chat/` | `user_message="",assistant_message="",mode="normal"` | `{success:true,conversation_id,pair_id}` |
| `/api/ai/records/save-evaluation/` | `selected_careers=[],evaluation_result={}` | `{success:true,record_id}` |
| `/api/ai/records/save-plan/` | `report_content="",radar_data={}` | `{success:true,report_id}` |
| `/api/ai/records/save-recommendation/` | `self_introduction="",recommendations=[]` | `{success:true,record_id}` |

共享聊天生成器已写聊天 pair；网页 save-chat 还用于用户归属/使用记录，重复保存会新增 pair，不作为“无副作用确认”调用。当前 legacy 聊天按 session_id 创建/获取会话时未完整校验已有会话归属，save-chat 还会重设会话用户；App 只复用自身新建的会话 ID，不暴露公共固定 session_id。这是现有后端权限缺口，不能把旧聊天链路标为已完成严格跨账号隔离验收。

### 9.3 历史查询与标签

| GET 路径 | 响应内容 |
| --- | --- |
| `/api/ai/records/user/` | `{success:true,records:[{id,usage_type,related_id,created_at,tags[]}]}` |
| `/api/ai/records/chat/` | `{success:true,conversations:[{id,session_id,mode,created_at,username,message_pairs[],tags[]}]}` |
| `/api/ai/records/evaluation/` | `{success:true,records:[{session_id,selected_careers,evaluation_result,created_at,tags[]}]}` |
| `/api/ai/records/plan/` | `{success:true,reports:[{session_id,report_content,radar_data,created_at,tags[]}]}` |
| `/api/ai/llm/chat/history/` | 必填 session_id、可选 limit 默认10；`{success:true,chat_history[],session_id,mode}` |

前四个接口无分页；chat 的 message_pairs 子项为 `input,input_time,output,output_time`，history 子项为 `id,user_input,ai_output,input_time,output_time`。history 当前按 input_time 升序取前 limit 项，不保证返回最近 limit 条；后端短期历史还可能清理旧 pair，不当作无限对话存档。

标签：`POST /api/ai/records/add-tag/`，`{record_id,record_type,tag_name}`，成功 `{success:true,tag_id}`。删除：`POST /api/ai/records/delete/`，`{record_id,record_type}`，成功 `{success:true}`。record_type 仅 `chat|evaluation|plan`，record_id 是**对应实体主键**，不是 usage record 的 id、related_id 或正式练习 UUID；不属于本人/不存在404。旧 evaluation/plan 列表未返回其实体 id，App 不能拿 session_id 猜测；若需要后续删除，可保存创建接口回传 ID，否则需后端补契约。

### 9.4 其他已挂载 AI 接口

| 方法与路径 | 请求/响应 |
| --- | --- |
| `GET /api/ai/llm/career/chunk/{record_id}/` | 返回 `{success,record,read_time,source,total_time}`，未找到404；依赖职业检索存储 |
| `POST /api/ai/llm/career/chunks/batch/` | `{record_ids:[...]}`；返回 `{success,records[],errors[],read_time,total_time}` |
| `POST /api/ai/llm/career/arag/` | `{self_introduction}`；返回 `{success,recommendation,session_id,response_time,raw_analysis}`；作为可选检索推荐路径 |
| `GET /api/ai/llm/memory/long-term/` | 可选 limit 默认10；返回 `{success:true,memories}`，记忆项由适配器决定 |
| `POST /api/ai/llm/memory/update/` | `{memory_id,value}`，两者必填/非空；返回 `{success:true}`，失败404 |
| `POST /api/ai/llm/memory/delete/` | `{memory_id}`；返回 `{success:true}`，失败404 |
| `GET /api/ai/health-check/` | `{success:true,status:"healthy",message}`；也需 Token，仅证明视图可响应，不证明模型可用 |
| `GET /api/ai/demo/career-recommendation/` | HTML 模板页面，非 App JSON 接口 |

这些历史接口不作为新练习链路必需依赖；不能从 URL 存在推断数据源、模型或记忆适配器在目标环境可用。

## 10. 简历 AI 优化预留接口

当前 `ResumeAnalysisService` 继承占位服务，无实际分析/应用实现。下列接口仅有路由、权限、输入校验和预留操作，**有效业务请求返回501 `FEATURE_NOT_IMPLEMENTED`**；详情可能先因记录不存在返回404。App 暂不显示可完成优化的承诺，不编造成功响应。

| 方法与路径 | 当前校验输入 |
| --- | --- |
| `GET /api/career-platform/resume-analyses/` | 通用 Query |
| `POST /api/career-platform/resume-analyses/` | 必填 `jd_text`（最大20000）、`resume_revision>=0`、UUID request_id；可选正整数 job_id |
| `GET /api/career-platform/resume-analyses/{id}/` | UUID id；通用 Query |
| `POST /api/career-platform/resume-analyses/{id}/apply/` | `base_revision>=0`、非空 UUID 数组 accepted_suggestion_ids、UUID request_id；可选 suggestion_edits 对象，值为最大20000字符文本 |

预留版本冲突/建议确认目标见[成员3实施文档](implementation/03-resume-optimization.md)，不能将这些输入字段当作并发确认业务已实现。

## 11. 管理员接口

普通 App 用户不调用这一组写操作，统一要求 Token；以下明确标注管理员的接口需要 is_staff。评估与导入是工具能力，不是用户面试评分。

### 11.1 正式目录批量导入

`POST /api/career-platform/catalog/imports/`，管理员。字段 `kind=companies|jobs|questions`，`records` 为1–1000个对象，`dry_run` 默认 true。先预览，再明确 `dry_run=false` 落库；成功200。

| kind | 核心必填内容 |
| --- | --- |
| companies | `source,name,city` |
| jobs | `source,title,company_id` 或企业名称 company；企业必须已存在；description/requirements 至少一项 |
| questions | `source,category,question,role,topic,difficulty,key_points,reference_answer` |

公共可选 `source_url,permission_status,collected_at,dedupe_key,is_demo`，许可状态 `pending|allowed|restricted`；新记录默认 pending，不会自动出现在 allowed 查询。支持的企业、岗位、题目字段见第5节与[数据目录说明](implementation/01-data-catalog.md)。records 子项不接受未知字段；本接口接 JSON 对象数组，不是上传文件接口。

返回 `{kind,dry_run,created,updated,unchanged,records:[{index,id,action}]}`；实际写入还可能带 `index_notification=scheduled|not_configured|not_required`。dry_run 新记录 id 可为 null；按 dedupe_key 去重，不接受客户端指定 ID 作为随意覆盖依据。先完成全批校验，有错误返回400，details 使用 `records[0].field` 等定位键。

### 11.2 模型/检索评估

| 方法与路径 | 用途 |
| --- | --- |
| `GET /api/career-platform/evaluation-runs/` | 当前管理员自己的评估记录，新课程分页 |
| `POST /api/career-platform/evaluation-runs/` | 创建评估；200 返回详情 |
| `GET /api/career-platform/evaluation-runs/{id}/` | UUID详情，仍限制本人记录 |
| `GET /api/career-platform/evaluation-runs/{id}/export/` | JSON导出，不是CSV/附件流，不重新跑评估 |

创建字段：`dataset_version`（最大100）、`task=job|resume|question`、`strategy`（最大100）、`parameters`（默认{}）、UUID request_id。当前只内置 question 评估集和 `text-baseline/text-balanced` 策略；task 枚举可接受 job/resume 不表示已有对应数据集。parameters 当前只接受 `k`（整数1–20，默认5），不接受 top_k/阈值等任意参数。

当前内置版本为 `java-eval-dev-v1`（调参）与 `java-eval-test-v1`（冻结测试）。示例：

```json
{"dataset_version":"java-eval-dev-v1","task":"question","strategy":"text-baseline","parameters":{"k":5},"request_id":"10000000-0000-4000-8000-000000000005"}
```

详情包含 `id,task,dataset_version,strategy,parameters,metrics,model_version,status,created_at,sample_count,failure_count,groups,request_id,results,metric_keys`；results 子项含 `query_id,query,group,relevant_ids,predicted_ids,metrics,failure`，可能带 unknown_ids。导出额外含 `contract_version,exported_at`。同 request_id 相同输入回放，不同输入409；未登记版本/策略400，全部样本执行失败503。

### 11.3 旧企业维护与建议审核

- `POST /api/company-reviews/companies/submit/`：管理员，`{company_name,ranking:"red|black",reason}`，成功200 `{success:true,message}`。
- `/api/company-reviews/companies/` 的 POST 和 `/{id}/` 的 PUT/PATCH/DELETE：管理员，使用旧 CompanySerializer 字段；标准创建201、更新200、删除204。
- `PATCH /api/ai/senior-advice/approve/{advice_id}/`：管理员，空 body 即可，成功200 `{success:true,message}`，不存在404、非管理员403。

职业分类/招聘会的标准写路由当前缺少同等 staff 限制，见第8节；本文不修改后端权限。

## 12. 错误、重试与联调检查

### 12.1 错误格式不能全局硬编码

新课程视图的业务错误通常为：

```json
{"code":"VALIDATION_ERROR","message":"Invalid input.","details":{"role":["该字段是必填项。"]}}
```

message 可能是框架默认英文或粗粒度信息，字段原因在 details 中。**统一 Token 中间件先执行**，因此新课程接口缺失/无效 Token 也可能直接返回 `{"detail":"Authentication credentials were not provided."}`，不带 code/message/details。

| HTTP | 常见 code/内容 | 客户端动作 |
| --- | --- | --- |
| 400 | VALIDATION_ERROR 或字段字典/non_field_errors/error | 标出字段问题，修正后再发 |
| 401 | detail；WWW-Authenticate: Token | 清除失效登录态，重新登录 |
| 403 | FORBIDDEN/detail/error | 提示权限不足；保留 Token |
| 404 | NOT_FOUND/detail/error | 记录不存在或无权访问；刷新列表 |
| 405 | METHOD_NOT_ALLOWED/error/detail | 核对方法、路径、末尾斜杠 |
| 409 | CONFLICT | 核对 request_id、当前状态、题目和回执，不无限重试 |
| 500 | INTERNAL_ERROR 或历史 error | 保留用户输入，提示稍后重试 |
| 501 | FEATURE_NOT_IMPLEMENTED | 展示待开发状态，不循环重试 |
| 502 | INVALID_MODEL_OUTPUT；ASR用result | 正式回答先查保存状态；其他按各接口处理 |
| 503 | MODEL_UNAVAILABLE/JOBS_UNAVAILABLE/JOB_FAIRS_UNAVAILABLE/ADVICE_UNAVAILABLE 等 | 服务/依赖不可用，不当作空数据成功 |
| 504 | MODEL_TIMEOUT；ASR用result | 判断输入是否已保存，再提供明确重试 |

普通 DRF 表单错误常为 `{"username":["..."]}`；旧AI常为 `{error}` 或 `{success:false,error}`；ASR为 `{result}`。统一展示层可依次读取 message/error/detail/result，再看字段字典；原始服务器异常不要作为面向用户的长错误文本显示。

### 12.2 App 请求恢复约定

1. 发送正式创建/作答前持久化业务 request_id 与待提交内容；同一操作网络重发保持同键同内容，不在每次 HTTP 重试时新建 UUID。
2. 回答/总结模型重试使用专用入口和新尝试键，区分“回答已保存”和“反馈已完成”；先查询详情再决定。
3. 旧保存接口、注册、投稿等没有统一幂等保障，超时后不要直接套用无限自动重发。读取与副作用请求采用不同策略。
4. 同步模型、ASR和SSE采用单独的可取消超时策略，不能照搬普通列表的短超时；客户端超时不等于服务端回滚。
5. 练习切换账号后清理对应缓存、UUID 与恢复队列；不复用另一账号回执/会话/答案ID。新课程记录的404不泄露归属信息。

### 12.3 最小联调检查

- 使用新账号注册/登录，保存 Token；GET profile 验证身份；退出后旧 Token 应401。
- 无简历404按首次创建处理；保存、再次读取，确认数组替换和头像路径。
- 先查目录，使用实际 role/topic/difficulty 与 ID；区分空库、无命中、503和演示数据。
- 正式检索后创建会话，按 presented_questions 作答；网络重发不重复保存；用另一账号读取应404。
- 用隔离替身验证反馈失败、回答保留、反馈重试和总结失败/重试；不要为接口联调默认调用付费模型。
- SSE 验证中文分块、多帧合块、音频可选、`[DONE]` 后 end、异常断流和取消；各平台真机另验录音/播放。
- 验证旧复盘列表只取10条、新练习列表分页、profile可写不可读字段、200空体和204不解析JSON。
- 目标环境的后端版本、数据库数据、模型配置和网关流式缓冲分别确认；代码核对不能代替部署验收。

## 13. 代码定位与维护

| 主题 | 当前拥有实现的文件 |
| --- | --- |
| 路由入口 | `backend/cs_career_guide/urls.py` 和各应用 `urls.py` |
| Token门禁 | `backend/cs_career_guide/middleware.py` |
| 用户资料/密码 | `backend/users/views.py`、`serializers.py`、`models.py` |
| 简历/数组保存 | `backend/resumes/views.py`、`serializers.py`、`models.py` |
| 新课程入参/错误 | `backend/career_platform/serializers.py`、`api.py`、`views/` |
| 目录与筛选 | `backend/career_platform/catalog/service.py`、`repository.py` |
| 正式检索/反馈 | `backend/career_platform/rag/service.py`、`retrieval.py` |
| 会话/幂等/总结 | `backend/career_platform/records/repository.py`、`service.py` |
| 服务组装/实现状态 | `backend/career_platform/composition.py`、`resume/service.py`、`evaluation/service.py` |
| 旧聊天/SSE/历史/复盘 | `backend/ai_integration/langgraph_views.py` |
| 推荐/报告/建议 | `backend/ai_integration/optimized_views.py`、`career_report_views.py`、`senior_advice_views.py` |
| ASR | `backend/asr/asr.py` |
| 网页调用参考 | `frontend/src/api/index.js`、`frontend/src/features/career-platform/api.js` |
| legacy提示词与解码 | `frontend/src/features/interview/legacy/implementation.js`、`stream.js` |

接口变更与本文一起更新；需求中的目标能力和实现中的现有行为分别标注。本文不引入接口、迁移、部署或客户端代码。发现的现有字段/权限/幂等局限记录在对应章节，后续修复需要单独实现与验证。
