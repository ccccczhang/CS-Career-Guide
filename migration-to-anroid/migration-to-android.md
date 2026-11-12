# CS Career Guide Android App UI/Mock 迁移方案

**文档版本**：V3.2  
**修订日期**：2026-11-12  
**当前状态**：Phase 1–4 UI/Mock 本地实现完成，等待真实桥接与真机验收  
**目标平台**：Android，Jetpack Compose  
**工程位置**：`C:\Users\Prologue\develop\work\migration-to-anroid`

## 1. 范围与原则

本阶段把现有 Vue 前端迁移为 Android Compose UI，只覆盖当前前端已经登记的 18 个路由。不新增 Web 端不存在的课程、社区、设置或企业评价路由。

- 数据全部来自本地 `MockRepository`，不发起网络请求。
- 登录、保存、导出、麦克风、流式回答和复盘均使用可观察的假状态。
- 不接真实后端、真实账号、模型、ASR、TTS 或数据库。
- 不把 Mock 成功展示为业务已经完成。
- 图片不是验收条件；缺少切图时使用色块、图标或占位图。
- UI 目标是移动端信息层级和交互一致，不要求逐像素复制桌面 Web。
- 当前实现按单列页面、`16dp` 页面边距、键盘避让和吸底主操作设计。

## 2. 技术路线

采用单 Activity、Jetpack Compose、Navigation Compose、MVVM 和 Repository 接口。

```text
Compose Screen
    ↓ events
ViewModel + immutable UiState
    ↓ interface
AppRepository
    ↓ implementation
InMemoryMockRepository
```

约束：

1. Screen 只负责展示和发送用户事件，不在 Composable 内写业务分支。
2. ViewModel 暴露不可变状态，统一区分 `Loading`、`Content`、`Empty`、`Error`。
3. Mock 放在 Repository 边界，不用假 HTTP 响应模拟“真实接口成功”。
4. 时间、随机数和延迟必须可注入或固定，保证测试可复现。
5. 第一阶段不引入数据库、依赖注入框架、网络库或图片加载库。

## 3. 全局导航

底部导航使用 5 个一级入口：

| Tab | 一级路由 | 说明 |
| --- | --- | --- |
| 首页 | `/` | 品牌、职业路径入口和功能快捷入口 |
| 规划 | `/career-paths` | 职业路径、自我介绍、职业选择和规划报告 |
| 求职 | `/company-reviews` | 企业与职位、红黑榜、建议表单和简历 |
| 学习 | `/interview` | AI 模拟面试和课程功能入口 |
| 我的 | `/users` | 个人资料、简历摘要、复盘入口和退出 |

登录、注册和所有二级页面不显示底部导航。二级页面必须提供明确返回路径。

第一阶段固定以下导航行为：

- 未登录启动：进入登录页。
- 登录成功：进入首页并清理登录/注册回退栈。
- 已登录访问受保护路由：允许进入。
- 未登录访问受保护路由：跳转登录页并保留原目标。
- 退出登录：清理 Mock 会话并返回登录页。
- 点击当前 Tab：不重复叠加同一个一级页面。

## 4. 18 路由覆盖矩阵

| 序号 | Web 路由 | Android 路由名 | 一级归属 | 当前状态 |
| --- | --- | --- | --- | --- |
| 1 | `/` | `home` | 首页 | Mock 页面完成 |
| 2 | `/login` | `login` | 认证 | Mock 登录完成 |
| 3 | `/register` | `register` | 认证 | Mock 注册完成 |
| 4 | `/career-paths` | `career_paths` | 规划 | Mock 页面完成 |
| 5 | `/career-paths/:type` | `career_path_detail/{type}` | 规划 | Mock 页面完成 |
| 6 | `/career-paths/:type/self-introduction` | `self_introduction/{type}` | 规划 | Mock 页面完成 |
| 7 | `/users` | `users` | 我的 | Phase 4 Mock 页面完成 |
| 8 | `/career-evaluation` | `career_evaluation` | 规划 | Mock 页面完成 |
| 9 | `/career-plan` | `career_plan` | 规划 | Mock 页面完成 |
| 10 | `/company-reviews` | `company_reviews` | 求职 | Phase 3 Mock 页面完成 |
| 11 | `/company-reviews/senior-advice/form` | `senior_advice_form` | 求职 | Phase 3 Mock 页面完成 |
| 12 | `/interview` | `interview` | 学习 | Phase 3 Mock 页面完成 |
| 13 | `/resume` | `resume` | 求职 | Phase 3 Mock 页面完成 |
| 14 | `/career-platform/catalog` | `platform_catalog` | 学习 | Phase 4 Mock 页面完成 |
| 15 | `/career-platform/resume` | `platform_resume` | 学习 | Phase 4 Mock 页面完成 |
| 16 | `/career-platform/practice` | `platform_practice` | 学习 | Phase 4 Mock 页面完成 |
| 17 | `/career-platform/records` | `platform_records` | 学习 | Phase 4 Mock 页面完成 |
| 18 | `/career-platform/evaluation` | `platform_evaluation` | 我的 | 管理员 Mock 页面完成 |

路由验收以代码中的 `RouteSpec` 清单为准。清单必须恰好包含 18 项，路由名唯一，Web 路径唯一。

### 4.1 实际界面差异评估（2026-11-12）

对照方式：使用 `390px` 宽无头浏览器截图核对 Vue 登录页，并在 `Medium_Phone_API_36.1` 模拟器安装 Debug APK，逐页检查登录、首页、企业与职位、简历及模拟面试。结论用于移动端信息架构验收，不等同于真机视觉或无碍验收。

- Web 顶部为品牌、七项文字导航和账号区；Android 使用五项底部导航。Android 将“职业路径、自我介绍、规划报告”归入“规划”，把“企业、简历”归入“求职”，这是移动端一级入口收敛，不是功能缺失。
- Web 登录页保留完整站点导航；Android 认证是独立导航栈，登录和注册页不显示底栏，减少未登录时无意义的页面切换。
- Web 首页强调品牌主视觉和模块展示；Android 首屏改为用户问候、下一步行动、四类职业路径和任务入口，信息密度更适合单手持握。两边视觉方向一致，但不逐像素复制。
- Phase 3 完成后，Android 企业页覆盖职位、红榜、黑榜、前辈经验和求职安全；简历页覆盖七章节、编辑/预览和假导出；面试页覆盖四风格、时长、对话、暂停、结束、复盘和假语音。
- Phase 4 已补齐课程功能和个人中心 UI，但真实岗位分页、外部申请链接、Markdown 渲染、真实 PDF、模型/SSE/ASR/TTS 和持久化仍未接入。页面均使用明确 `MOCK` 标识，不能把交互通过视为真实业务完成。

## 5. 页面迁移要求

### 5.1 首页与认证

- 首页展示当前 Mock 用户、主视觉卡片、四个职业方向和核心功能入口。
- 登录页包含账号、密码、加载状态、错误提示和注册入口。
- 注册页校验非空、密码长度和两次密码一致。
- Phase 1 使用假账号，任意符合前端校验的输入均可登录。

### 5.2 规划

- 职业路径列表使用两列卡片，点击进入职业详情。
- 自我介绍使用三章节小步骤条、单列表单、技能分类折叠、草稿状态和吸底“下一步”。
- 职业选择包含搜索、领域筛选、横向推荐卡片、职业列表和吸底选择栏。
- 规划报告使用可滚动的分区卡片；长报告不在 Phase 1 实现。

### 5.3 求职与简历

- 企业页包含职位、红榜、黑榜、求职安全和学长建议等 Tab。
- 搜索、分页、空状态、加载和失败状态必须可从 Mock 场景切换。
- 简历页使用编辑/预览 Tab；编辑包含 7 个章节，预览只读取当前草稿。
- 导出只返回“Mock 导出成功”，不生成真实 PDF。

### 5.4 学习与课程功能

- 面试页包含四种风格、时长选择、对话、暂停、结束、复盘和假语音按钮。
- 流式回复用本地延迟文本模拟，不接 SSE。
- 麦克风只展示权限申请和转写中的假状态，不访问真实设备。
- `/career-platform/*` 保持现有“岗位与题库、简历优化、岗位题库练习、练习记录、评估与调优”的含义。
- `/career-platform/evaluation` 是管理员工具，不作为普通用户学习内容。

### 5.5 我的

- 展示用户资料、自我介绍、职业推荐、简历摘要和复盘记录入口。
- 编辑资料使用可关闭的 BottomSheet 或 Dialog。
- 退出登录后清除所有界面会话状态。

## 6. Mock 数据与状态

第一阶段至少登记以下 Kotlin 数据：

- `UserProfile`
- `CareerPathSummary`
- `SkillGroup`
- `CareerCategory`
- `Recommendation`
- `JobSummary`
- `CompanyRanking`
- `SeniorAdvice`
- `ResumeDraft`
- `InterviewConfig`
- `InterviewReview`
- `CourseItem`
- `PracticeRecord`

每个列表页面需要可切换场景：

- `Content`：正常内容。
- `Loading`：加载中。
- `Empty`：真实空结果。
- `Error`：可重试失败。

Phase 1 完成首页和认证；Phase 2 已接入 24 项技能、9 个职业领域、62 个岗位、Mock 推荐和规划报告。

## 7. 测试策略

### 7.1 Phase 1 自动化测试

- 路由清单恰好 18 项且唯一。
- 公开路由仅包含登录和注册。
- 未登录启动进入登录页。
- 合格输入可完成假登录并返回首页。
- 退出登录后回到登录页。
- Mock Repository 返回稳定用户和四个职业方向。
- ViewModel 区分登录中、登录成功和校验失败。

### 7.2 后续 Compose 测试

- 底部导航切换和当前 Tab 状态。
- 表单输入、错误提示、加载禁用和键盘避让。
- Tab、Accordion、BottomSheet、Dialog 和吸底操作栏。
- 内容、空、加载、失败四种场景。
- `320dp`、`360dp`、`390dp`、`412dp` 宽度和字体放大。

### 7.3 验收命令

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

自动化通过只代表 UI/Mock 工程可构建和逻辑可验证，不代表真机、无障碍、真实账号、PDF、麦克风或模型验收。

## 8. 实施阶段

### Phase 1：工程、导航、认证和 Mock 基础

- 修订本方案。
- 建立 Android Compose 单模块工程。
- 建立 18 路由清单和 5 个底部入口。
- 实现首页骨架、假登录、假注册和退出。
- 建立 Repository 契约、内存实现和基础领域模型。
- 补齐路由、Repository、ViewModel 单元测试。

### Phase 2：规划链路

- **已完成**：职业路径列表和详情。
- **已完成**：自我介绍、技能评估、自动保存和吸底下一步。
- **已完成**：职业选择、推荐卡片和规划报告。

### Phase 3：求职、简历和面试

- **已完成**：企业职位、榜单、安全、前辈经验与本地经验提交。
- **已完成**：七章节简历编辑、预览、假保存和假导出。
- **已完成**：面试配置、Mock 对话、暂停、结束、复盘和假语音状态。

### Phase 4：课程功能与验收

- **已完成**：5 个 `/career-platform/*` UI/Mock 页面。
- **已完成**：个人资料编辑、推荐、简历摘要、复盘和练习入口。
- **已完成**：本地空结果、表单校验、操作加载、管理员提示和桥接占位。
- **已完成**：`320dp` 宽度、`1.3x` 字体缩放的模拟器流程回归。
- 待完成：真实接口联调、多厂商真机截图和无障验收。

## 9. 已知边界

- 当前 Web 前端没有独立的完整课程、社区、设置和企业评价路由，本阶段不为它们新增路由。
- `/career-platform/*` 已完成 Compose UI/Mock，但真实分页、模型、权限和持久化仍依赖后端桥接。
- 图片和视觉稿缺失不阻塞迁移，但后续视觉验收需要提供可选素材或接受占位设计。
- 浏览器 `localStorage`、PDF、麦克风和流式网络能力不直接迁移，使用 Android 对应抽象并在 Mock 阶段返回假结果。
- 真实桥接的路由、数据、权限、幂等、安全和验收前置条件见 [桥接说明](docs/bridge-integration.md)；代码接入点使用 `TODO(bridge)` 标记。

## 10. Phase 1 实施记录

记录日期：2026-11-10。

已实现：

- Android Compose 单模块工程、Gradle Wrapper 和 Debug APK。
- 单 Activity、Navigation Compose、MVVM 和 `AppRepository`。
- 18 个 Web 路由对应的 Android 路由清单。
- 5 个底部入口以及登录、注册、首页、个人中心骨架。
- 本地假登录、假注册、退出和登录拦截。
- `InMemoryMockRepository`、稳定示例用户和四条职业方向。
- 后续页面所需领域模型已登记。

本地验证：

- `testDebugUnitTest`：12 项通过，0 失败。
- `assembleDebug`：通过。
- `lintDebug`：0 error；7 个 warning 均为固定兼容版本的更新提示。

验证边界：

- 当前未连接 Android 设备或模拟器，未执行安装、启动、触控、键盘、字体放大和真机截图验收。
- 仅首页、登录、注册和路由骨架完成；业务页面仍明确标记为 Phase 1。
- 未接网络、真实账号、数据库、模型、ASR/TTS、真实麦克风或 PDF。
- 因本机最高为 API 36/AGP 8.13，暂时固定 Navigation Compose `2.9.8`；`2.10.x` 要求 API 37/AGP 9.1，不作为当前缺陷。

## 11. Phase 2 实施记录

记录日期：2026-11-12。

已实现：

- 职业路径列表和四类路径详情。
- 个人自述：三章节、单列表单、24 项技能、分类折叠、草稿状态和吸底下一步。
- 职业方向选择：Mock 推荐卡片、搜索、9 个领域筛选、62 个岗位和吸底选择栏。
- 职业规划报告：人才画像、市场与城市、成长路线、学习路径、求职风险及近期计划。
- `PlanningViewModel`、本地推荐生成和规划报告生成。

导航缺陷修复：

- 问题：从规划等非首页 Tab 无法返回首页。
- 原因：底部栏回调捕获了旧的 `currentRoute` 快照，首页分支被提前返回。
- 修复：认证流与主应用 NavHost 分离；登录后主 NavHost 固定从首页启动；底部栏点击时读取 NavController 的实时目的地。
- 回归：模拟器依次进入规划、求职、学习、我的并返回首页，全部通过。

本地验证：

- `testDebugUnitTest`：17 项通过，0 失败。
- `connectedDebugAndroidTest`：2 项通过，覆盖底部 Tab 返回首页和完整规划流程。
- `assembleDebug`：通过。
- `lintDebug`：0 error；warning 只包含固定兼容依赖的版本更新提示。

验证边界：

- 当前为 Android 模拟器验证，不代表不同厂商真机、系统字体放大和无障碍验收。
- 推荐、职业匹配度、薪资和规划内容均为本地 Mock，不是模型或真实市场结果。
- 草稿只保存在当前应用内存会话中，退出应用后不保证恢复。

## 12. Phase 3 实施记录

记录日期：2026-11-12。

已实现：

- 企业页：职位搜索、红榜、黑榜、前辈经验和求职安全五个栏目。
- 前辈经验表单：必填校验、评分、优缺点要点和本地提交成功状态；提交后可回到经验列表查看。
- 简历工作台：基本信息、个人自评、教育背景、实习经历、项目经验、专业技能和证书认证七个章节。
- 简历编辑/预览切换、条目增删、本地保存和 Mock PDF 导出提示。
- 模拟面试：温和面、技术面、压力面和行为面；10/20/30/45 分钟；本地计时、暂停、结束和 Mock 复盘。
- `CareerPrepViewModel` 统一管理求职、简历和面试状态；新增 `MockCareerPrepData` 离线演示数据。

本地验证：

- `testDebugUnitTest`：21 项通过，0 失败。
- `connectedDebugAndroidTest`：3 项通过，覆盖底部导航、完整规划流程，以及企业页、简历预览和模拟面试主链路。
- `assembleDebug`：通过。
- `lintDebug`：0 error；源码无新增 lint error，剩余 warning 为固定兼容依赖的版本升级提示。
- 模拟器人工截图检查：登录、企业职位列表、简历编辑、简历预览、面试配置、面试对话和复盘页均可正常滚动与点击。

验证边界：

- 岗位搜索只在本地 5 条示例数据中过滤，没有分页、真实申请链接或服务端搜索。
- 经验、简历、面试对话和复盘只保存在当前内存会话；重启应用后不保证恢复。
- 简历预览不做 Markdown 渲染，PDF 导出不生成真实文件。
- 面试不访问模型、麦克风、ASR 或 TTS；语音开关仅展示 Mock 状态。
- 当前仅为 API 36 模拟器验收，不代表不同厂商真机、字体放大、无障碍或复杂键盘场景。

## 13. Phase 4 实施记录

记录日期：2026-11-12。

已实现：

- 岗位与题库：岗位、企业、题库切换，关键词/城市/方向/知识点/难度筛选，本地分页语义和来源许可展示。
- 简历优化：JD 输入、简历版本、Mock 分析、原文证据、建议多选和确认应用状态。
- 岗位题库练习：检索条件、场景预设、候选题目、匹配值、参考要点和来源证据展开；空结果明确展示。
- 练习记录：关键词/岗位/知识点/难度筛选、历史列表、详情、逐题回答、反馈和总结失败状态。
- 评估与调优：管理员权限提示、评估集/策略/Top-K、指标卡、最近记录和 Mock JSON 导出。
- 个人中心：资料编辑弹窗、职业推荐、自我介绍摘要、简历摘要、面试复盘、练习记录、管理员入口和退出。
- `WorkspaceViewModel`、`MockWorkspaceData` 和 `WorkspaceModels`；新增 5 个课程 UI 页面测试覆盖。
- `CareerBridgeContract.kt` 登记未来接口路由与适配器边界，关键接入点使用 `TODO(bridge)`。

桥接文档：

- 新增 [桥接说明](docs/bridge-integration.md)，列出环境、Token、路由、分页、字段、请求幂等、权限、SSE、PCM 上传、测试账号和验收清单。
- 当前 `MainActivity` 仍注入 `InMemoryMockRepository`；真实 HTTP adapter 尚未实现或调用。

本地验证：

- `testDebugUnitTest`：27 项通过，0 失败。
- `connectedDebugAndroidTest`：4 项通过，覆盖底部导航、规划、求职链路、Phase 4 五页面和个人资料编辑入口。
- 小屏回归：在 `920x2040` 物理像素、覆盖密度 `460dpi`（约 `320dp` 宽）和 `1.3x` 字体缩放下，上述 4 项 UI 测试再次全部通过。
- `assembleDebug`：通过。
- `lintDebug`：通过；剩余 warning 为固定兼容依赖的版本更新提示。
- 模拟器人工检查：个人中心、课程导航、目录筛选、简历优化、题库检索、练习记录和评估页可滚动、点击和返回。

验证边界：

- 所有数据仍来自内存 Mock；没有真实后端、模型、SSE、ASR/TTS、PDF、头像上传或跨设备持久化。
- 管理员评估只演示 UI 和指标格式，不代表真实评估集、指标或权限结果。
- 岗位/题库搜索是本地过滤，没有服务端分页、巨大数据量性能或来源合规验收。
- `320dp` 和 `1.3x` 字体缩放已通过模拟器自动检查；多厂商真机、横屏和无障碍仍需真实设备验收。
