package com.cscareerguide.app.data

import com.cscareerguide.app.data.bridge.PracticeRetrieveRequest
import com.cscareerguide.app.data.bridge.InterviewChatRequest
import com.cscareerguide.app.data.bridge.InterviewStreamEvent
import com.cscareerguide.app.data.bridge.InterviewStreamResult
import com.cscareerguide.app.data.model.CareerPathSummary
import com.cscareerguide.app.data.model.CareerCategory
import com.cscareerguide.app.data.model.CareerPlanReport
import com.cscareerguide.app.data.model.CatalogItem
import com.cscareerguide.app.data.model.CitySuggestion
import com.cscareerguide.app.data.model.CompanyRanking
import com.cscareerguide.app.data.model.EvaluationRun
import com.cscareerguide.app.data.model.HomeMetrics
import com.cscareerguide.app.data.model.IntroductionDraft
import com.cscareerguide.app.data.model.InterviewConfig
import com.cscareerguide.app.data.model.InterviewReview
import com.cscareerguide.app.data.model.JobSummary
import com.cscareerguide.app.data.model.LearningStage
import com.cscareerguide.app.data.model.PracticeQuestion
import com.cscareerguide.app.data.model.PracticeRetrieval
import com.cscareerguide.app.data.model.PracticeRecordDetail
import com.cscareerguide.app.data.model.Recommendation
import com.cscareerguide.app.data.model.ResumeAnalysis
import com.cscareerguide.app.data.model.ResumeDraft
import com.cscareerguide.app.data.model.RoadmapPhase
import com.cscareerguide.app.data.model.SalaryForecast
import com.cscareerguide.app.data.model.SeniorAdvice
import com.cscareerguide.app.data.model.SeniorAdviceDraft
import com.cscareerguide.app.data.model.SkillGroup
import com.cscareerguide.app.data.model.UserProfile
import com.cscareerguide.app.data.model.WeeklyTask

class InMemoryMockRepository : AppRepository {
    private var sessionUser: UserProfile? = null
    private var savedIntroduction = IntroductionDraft()
    private var savedResume = MockCareerPrepData.resume
    private val savedAdvice = MockCareerPrepData.initialAdvice.toMutableList()

    private val demoUser = UserProfile(
        id = "mock-user-001",
        username = "demo_student",
        displayName = "示例同学",
        school = "示例大学",
        major = "计算机科学与技术",
        grade = "大三",
        profile = "关注后端开发与工程实践，希望完成一次完整的职业规划。",
        education = "本科",
        email = "student@example.com",
        address = "长沙",
    )

    private val paths = listOf(
        CareerPathSummary(
            id = "employment",
            title = "就业",
            subtitle = "进入企业工作",
            description = "从实习、项目和技术面试开始，建立可验证的求职准备。",
            mark = "01",
        ),
        CareerPathSummary(
            id = "postgraduate",
            title = "考研",
            subtitle = "继续学术深造",
            description = "比较院校与研究方向，形成专业课和复习节奏。",
            mark = "02",
        ),
        CareerPathSummary(
            id = "civil_service",
            title = "考公",
            subtitle = "进入公共服务岗位",
            description = "了解考试科目、岗位限制和长期职业要求。",
            mark = "03",
        ),
        CareerPathSummary(
            id = "entrepreneurship",
            title = "创业",
            subtitle = "验证产品与技术想法",
            description = "从问题验证、团队协作到最小产品，逐步检验方向。",
            mark = "04",
        ),
    )

    override fun currentUser(): UserProfile? = sessionUser

    override fun careerPaths(): List<CareerPathSummary> = paths

    override fun homeMetrics(): HomeMetrics = HomeMetrics(
        careerPathCount = paths.size,
        recommendationRate = 98,
    )

    override fun skillGroups(): List<SkillGroup> = MockPlanningData.skillGroups

    override fun careerCategories(): List<CareerCategory> = MockPlanningData.careerCategories

    override fun introductionDraft(): IntroductionDraft = savedIntroduction

    override fun saveIntroduction(draft: IntroductionDraft) {
        savedIntroduction = draft
    }

    override fun recommendations(draft: IntroductionDraft): List<Recommendation> {
        val skills = draft.skills
        val target = draft.goal
        val backend = when {
            skills.any { it.contains("Python") } -> 92
            skills.any { it.contains("PostgreSQL") || it.contains("RESTful") } -> 88
            else -> 80
        }
        val data = if (skills.any { it.contains("数据分析") || it.contains("机器学习") }) 86 else 74
        val algorithm = if (skills.any { it.contains("算法") || it.contains("深度学习") || it.contains("NLP") }) 89 else 72
        return listOf(
            Recommendation(
                career = target.ifBlank { "后端工程师" },
                matchScore = 95,
                reason = "目标明确，并结合了当前技能和项目经历。",
            ),
            Recommendation(
                career = "Python 后端 / 全栈开发",
                matchScore = backend,
                reason = "已有开发、数据库或接口相关基础，适合继续补强工程项目。",
            ),
            Recommendation(
                career = "数据分析师",
                matchScore = data,
                reason = "数据分析能力与业务沟通可以形成互补，适合作为相近方向。",
            ),
            Recommendation(
                career = "算法工程师",
                matchScore = algorithm,
                reason = "数学与人工智能技能可以继续转化为可验证的算法项目。",
            ),
        ).distinctBy(Recommendation::career).sortedByDescending(Recommendation::matchScore)
    }

    override fun careerPlan(draft: IntroductionDraft, targetCareer: String): CareerPlanReport {
        val strengths = buildList {
            if (draft.skills.isNotEmpty()) add("已梳理 ${draft.skills.size} 项技能")
            if (draft.otherSkills.isNotBlank()) add("有可继续展开的项目或实践经历")
            if (draft.goal.isNotBlank()) add("职业目标较明确")
        }.ifEmpty { listOf("已开始建立个人职业画像") }
        return CareerPlanReport(
            targetCareer = targetCareer,
            talentPortrait = "你具备计算机专业背景，正在从课程能力转向可持续展示的工程能力。当前目标围绕 $targetCareer 展开。",
            personalityFit = "偏好结构化解决问题，适合在明确目标下持续迭代，并通过项目结果验证学习效果。",
            strengths = strengths,
            weaknesses = listOf("项目证据仍需补充", "岗位要求与个人技能还需要逐项对齐"),
            marketHeat = "中高",
            salaryForecast = listOf(
                SalaryForecast("第 1 年", "8–15K", "取决于项目完整度和实习经历"),
                SalaryForecast("第 3 年", "15–25K", "取决于独立交付和技术深度"),
                SalaryForecast("第 5 年", "25K+", "取决于业务影响与团队责任"),
            ),
            citySuggestions = listOf(
                CitySuggestion("长沙", 86, "生活成本可控，本地岗位与校园资源可结合。"),
                CitySuggestion("深圳", 82, "岗位密度高，适合积累项目与面试经验。"),
                CitySuggestion("杭州", 80, "互联网与数据岗位集中，竞争也较高。"),
            ),
            roadmap = listOf(
                RoadmapPhase(
                    "01 / 夯实基础",
                    "形成一份可投递的技术简历",
                    listOf("补齐计算机核心课程", "完成一个可运行项目", "整理项目中的职责和结果"),
                ),
                RoadmapPhase(
                    "02 / 对准岗位",
                    "完成一轮目标岗位面试准备",
                    listOf("拆解 10 份目标 JD", "建立知识与问题清单", "进行模拟面试和复盘"),
                ),
                RoadmapPhase(
                    "03 / 开始求职",
                    "形成稳定投递和复盘节奏",
                    listOf("每周固定投递", "记录面试问题", "根据反馈迭代简历和项目"),
                ),
            ),
            learningPath = listOf(
                LearningStage(
                    "基础阶段",
                    listOf("数据结构", "操作系统", "计算机网络", "数据库"),
                    "课程知识索引与练习记录",
                ),
                LearningStage(
                    "工程阶段",
                    listOf("接口设计", "测试", "Git", "部署"),
                    "一个包含前后端和数据持久化的完整项目",
                ),
                LearningStage(
                    "面试阶段",
                    listOf("项目表达", "算法", "系统设计", "行为问题"),
                    "项目复盘文档与模拟面试记录",
                ),
            ),
            jobHuntingStrategy = listOf(
                "按城市、行业和岗位方向建立三层目标名单。",
                "每份投递保留 JD 快照，并针对关键词调整简历。",
                "面试后 24 小时内记录问题和改进项。",
            ),
            riskAlerts = listOf(
                "避免只收藏资料而没有可展示成果。",
                "不要把课程练习包装成真实业务经历。",
                "投递前核实企业信息，不向招聘方支付费用。",
            ),
            weeklyFocus = "完成项目主线并整理岗位关键词",
            weeklySchedule = listOf(
                WeeklyTask("周一", "拆解 3 份目标 JD", 2),
                WeeklyTask("周三", "推进项目核心功能", 4),
                WeeklyTask("周五", "整理项目证据与简历", 2),
                WeeklyTask("周日", "复盘本周并安排下周", 1),
            ),
            monthlyTheme = "从学习记录转向作品和面试表达",
            monthlyTargets = listOf(
                "完成 1 个可演示项目",
                "形成 1 份针对目标的简历",
                "完成 2 次模拟面试",
            ),
        )
    }

    override fun jobs(): List<JobSummary> = MockCareerPrepData.jobs

    override fun companyRankings(isPositive: Boolean): List<CompanyRanking> =
        if (isPositive) MockCareerPrepData.redRankings else MockCareerPrepData.blackRankings

    override fun seniorAdvice(): List<SeniorAdvice> = savedAdvice.toList()

    override fun addSeniorAdvice(draft: SeniorAdviceDraft): SeniorAdvice {
        val advice = SeniorAdvice(
            id = "advice-${savedAdvice.size + 1}",
            company = draft.company.trim(),
            position = draft.position.trim(),
            author = draft.author.trim(),
            graduationYear = draft.graduationYear.trim(),
            rating = draft.rating.toDoubleOrNull() ?: 0.0,
            content = draft.advice.trim(),
            currentCompany = draft.currentCompany.trim(),
            salaryInfo = draft.salaryInfo.trim(),
            pros = splitPoints(draft.pros),
            cons = splitPoints(draft.cons),
        )
        savedAdvice.add(0, advice)
        return advice
    }

    override fun resumeDraft(): ResumeDraft = savedResume

    override fun saveResume(draft: ResumeDraft) {
        savedResume = draft
    }

    override fun interviewConfig(): InterviewConfig = MockCareerPrepData.interviewConfig

    override fun interviewReview(style: String): InterviewReview = InterviewReview(
        id = "review-001",
        style = style,
        score = 78,
        summary = "回答能覆盖项目背景和具体行动，技术取舍与结果验证还可以更具体。",
        strengths = listOf("项目背景交代清楚", "能说明自己的职责边界", "回答节奏稳定"),
        improvements = listOf("量化结果偏少", "对备选方案比较不足", "遇到追问时需要先确认问题范围"),
        nextActions = listOf(
            "为项目补充一个可验证的结果指标",
            "按“背景—任务—行动—结果”重写一次项目介绍",
            "完成一轮技术面追问练习",
        ),
    )

    // TODO(bridge): replace these workspace reads and writes with the HTTP adapter
    // described in docs/bridge-integration.md once authentication and test data are ready.
    override fun catalogItems(): List<CatalogItem> = MockWorkspaceData.catalog

    override fun resumeAnalysis(revision: Int): ResumeAnalysis =
        MockWorkspaceData.resumeAnalysis.copy(
            id = "analysis-${revision.coerceAtLeast(0) + 1}",
            resumeRevision = revision,
        )

    override fun practiceQuestions(): List<PracticeQuestion> = MockWorkspaceData.practiceQuestions

    override suspend fun retrievePracticeQuestions(
        request: PracticeRetrieveRequest,
    ): AppResult<PracticeRetrieval> {
        val results = MockWorkspaceData.practiceQuestions
            .filter { it.role == request.role }
            .filter { request.topic.isBlank() || it.topic.contains(request.topic, ignoreCase = true) }
            .filter { request.difficulty.isBlank() || it.difficulty == request.difficulty }
            .filter {
                request.jdText.isBlank() ||
                    it.question.contains(request.jdText, ignoreCase = true) ||
                    it.topic.contains(request.jdText, ignoreCase = true)
            }
            .take(request.topK.coerceIn(1, 20))
        return AppResult.Success(
            PracticeRetrieval(
                count = results.size,
                retrievalId = "mock-retrieval-${request.requestId}",
                message = if (results.isEmpty()) "没有符合条件的题目，请调整条件。" else "",
                results = results,
            ),
        )
    }

    override suspend fun streamInterviewChat(
        request: InterviewChatRequest,
        onEvent: (InterviewStreamEvent) -> Unit,
    ): AppResult<InterviewStreamResult> {
        val content = when {
            request.mode == "interview_review" ->
                "面试表现总体清晰。后续应补充技术取舍、验证结果和失败后的恢复方式。"
            request.message.contains("用户的回答是") ->
                "你提到了项目经历。请继续说明当时比较过哪些方案，以及为什么最终选择现在的实现。"
            else -> "请先介绍你的技术栈，并选一个最熟悉的项目说明职责和结果。"
        }
        onEvent(InterviewStreamEvent.Chunk(content))
        onEvent(InterviewStreamEvent.Done)
        onEvent(InterviewStreamEvent.Session("mock-interview-session"))
        return AppResult.Success(
            InterviewStreamResult(
                content = content,
                sessionId = "mock-interview-session",
                done = true,
                ended = true,
            ),
        )
    }

    override fun practiceRecords(): List<PracticeRecordDetail> = MockWorkspaceData.records

    override fun evaluationRuns(): List<EvaluationRun> = MockWorkspaceData.evaluations

    override fun evaluationRun(
        datasetVersion: String,
        strategy: String,
        topK: Int,
    ): EvaluationRun {
        val base = MockWorkspaceData.evaluations.first()
        return base.copy(
            id = "eval-preview",
            datasetVersion = datasetVersion,
            strategy = strategy,
            topK = topK,
            createdAt = "刚刚",
        )
    }

    override suspend fun loadResume(): AppResult<ResumeDraft?> =
        AppResult.Success(savedResume)

    override suspend fun saveResumeRemote(draft: ResumeDraft): AppResult<ResumeDraft> {
        savedResume = draft
        return AppResult.Success(draft)
    }

    override suspend fun loadCatalog(query: CatalogQuery): AppResult<PagedData<CatalogItem>> {
        val filtered = catalogItems().filter { item ->
            item.kind == query.kind &&
                matches(query.query, item.title, item.subtitle, item.description, item.source) &&
                matches(query.city, item.city) &&
                matches(query.role, item.role, item.subtitle, item.tags.joinToString()) &&
                matches(query.topic, item.topic, item.title) &&
                (query.difficulty.isBlank() || item.difficulty == query.difficulty)
        }
        return AppResult.Success(
            PagedData(
                items = filtered,
                total = filtered.size,
                page = query.page,
                pageSize = query.pageSize,
            ),
        )
    }

    override suspend fun loadJobs(
        search: String,
        page: Int,
        pageSize: Int,
    ): AppResult<PagedData<JobSummary>> {
        val filtered = jobs().filter { job ->
            matches(search, job.title, job.company, job.city, job.industry, job.description)
        }
        return AppResult.Success(PagedData(filtered, filtered.size, page, pageSize))
    }

    override suspend fun loadSeniorAdvice(
        search: String,
        ordering: String,
        page: Int,
        pageSize: Int,
    ): AppResult<PagedData<SeniorAdvice>> {
        val filtered = seniorAdvice()
            .filter { matches(search, it.company, it.position, it.content) }
            .let { items ->
                if (ordering == "rating") items.sortedByDescending(SeniorAdvice::rating) else items
            }
        return AppResult.Success(PagedData(filtered, filtered.size, page, pageSize))
    }

    override suspend fun analyzeResumeRemote(
        jdText: String,
        revision: Int,
        requestId: String,
    ): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun applyResumeAnalysisRemote(
        analysisId: String,
        baseRevision: Int,
        suggestionIds: List<String>,
        requestId: String,
    ): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun updateProfile(user: UserProfile): AuthOutcome {
        sessionUser = user
        return AuthOutcome.Success(user)
    }

    override suspend fun restoreSession(): AuthOutcome? =
        sessionUser?.let(AuthOutcome::Success)

    override suspend fun login(username: String, password: String): AuthOutcome {
        val normalizedUsername = username.trim()
        return when {
            normalizedUsername.isBlank() -> AuthOutcome.Failure("请输入账号。")
            password.length < 6 -> AuthOutcome.Failure("密码至少需要 6 位。")
            else -> {
                sessionUser = demoUser.copy(username = normalizedUsername)
                AuthOutcome.Success(sessionUser!!)
            }
        }
    }

    override suspend fun register(
        username: String,
        password: String,
        confirmation: String,
    ): AuthOutcome {
        val normalizedUsername = username.trim()
        return when {
            normalizedUsername.isBlank() -> AuthOutcome.Failure("请输入账号。")
            password.length < 6 -> AuthOutcome.Failure("密码至少需要 6 位。")
            password != confirmation -> AuthOutcome.Failure("两次输入的密码不一致。")
            else -> {
                sessionUser = demoUser.copy(
                    username = normalizedUsername,
                    displayName = normalizedUsername,
                )
                AuthOutcome.Success(sessionUser!!)
            }
        }
    }

    override suspend fun logout() {
        sessionUser = null
    }

    fun clearUserState() {
        sessionUser = null
        savedIntroduction = IntroductionDraft()
        savedResume = ResumeDraft()
        savedAdvice.clear()
    }

    fun resetSessionUser(user: UserProfile) {
        sessionUser = user
    }

    private fun splitPoints(value: String): List<String> = value
        .split(',', '，')
        .map(String::trim)
        .filter(String::isNotBlank)

    private fun matches(query: String, vararg values: String): Boolean =
        query.isBlank() || values.any { it.contains(query.trim(), ignoreCase = true) }
}
