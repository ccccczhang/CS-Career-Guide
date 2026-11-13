package com.cscareerguide.app.ui.careerprep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cscareerguide.app.data.AppRepository
import com.cscareerguide.app.data.AppResult
import com.cscareerguide.app.data.bridge.InterviewChatRequest
import com.cscareerguide.app.data.bridge.InterviewStreamEvent
import com.cscareerguide.app.data.model.CompanyRanking
import com.cscareerguide.app.data.model.InterviewMessage
import com.cscareerguide.app.data.model.InterviewReview
import com.cscareerguide.app.data.model.InterviewSpeaker
import com.cscareerguide.app.data.model.JobSummary
import com.cscareerguide.app.data.model.ResumeCertificate
import com.cscareerguide.app.data.model.ResumeDraft
import com.cscareerguide.app.data.model.ResumeEducation
import com.cscareerguide.app.data.model.ResumeExperience
import com.cscareerguide.app.data.model.ResumeProject
import com.cscareerguide.app.data.model.SeniorAdvice
import com.cscareerguide.app.data.model.SeniorAdviceDraft
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class HuntingTab(val label: String) {
    JOBS("职位"),
    RED("红榜"),
    BLACK("黑榜"),
    ADVICE("前辈经验"),
    SAFETY("求职安全"),
}

enum class ResumePane {
    EDIT,
    PREVIEW,
}

enum class ResumeSection(val label: String) {
    BASIC("基本信息"),
    EVALUATION("个人自评"),
    EDUCATION("教育背景"),
    EXPERIENCE("实习经历"),
    PROJECTS("项目经验"),
    SKILLS("专业技能"),
    CERTIFICATES("证书认证"),
}

data class AdviceFormUiState(
    val draft: SeniorAdviceDraft = SeniorAdviceDraft(),
    val submitting: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null,
)

data class HuntingUiState(
    val selectedTab: HuntingTab = HuntingTab.JOBS,
    val search: String = "",
    val jobs: List<JobSummary> = emptyList(),
    val positiveRankings: List<CompanyRanking> = emptyList(),
    val negativeRankings: List<CompanyRanking> = emptyList(),
    val advice: List<SeniorAdvice> = emptyList(),
    val form: AdviceFormUiState = AdviceFormUiState(),
    val loading: Boolean = false,
    val error: String? = null,
) {
    val visibleJobs: List<JobSummary>
        get() = jobs.filter { job ->
            search.isBlank() || listOf(
                job.title,
                job.company,
                job.city,
                job.industry,
                job.description,
            ).any { it.contains(search, ignoreCase = true) }
        }

    val visibleAdvice: List<SeniorAdvice>
        get() = advice.filter { item ->
            search.isBlank() || listOf(item.company, item.position, item.content)
                .any { it.contains(search, ignoreCase = true) }
        }
}

data class ResumeUiState(
    val draft: ResumeDraft = ResumeDraft(),
    val pane: ResumePane = ResumePane.EDIT,
    val section: ResumeSection = ResumeSection.BASIC,
    val saving: Boolean = false,
    val notice: String? = null,
    val noticeIsError: Boolean = false,
)

data class InterviewUiState(
    val styleId: String = "technical",
    val durationMinutes: Int = 20,
    val remainingSeconds: Int = 20 * 60,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isLoading: Boolean = false,
    val messages: List<InterviewMessage> = emptyList(),
    val voiceMockEnabled: Boolean = false,
    val review: InterviewReview? = null,
    val reviewLoading: Boolean = false,
    val sessionId: String = "",
    val error: String? = null,
    val reviewProgress: String = "",
)

class CareerPrepViewModel(
    private val repository: AppRepository,
    private val mockDelayMillis: Long = 450L,
) : ViewModel() {
    private val config = repository.interviewConfig()

    private val _huntingState = MutableStateFlow(
        HuntingUiState(
            jobs = repository.jobs(),
            positiveRankings = repository.companyRankings(true),
            negativeRankings = repository.companyRankings(false),
            advice = repository.seniorAdvice(),
        ),
    )
    val huntingState: StateFlow<HuntingUiState> = _huntingState.asStateFlow()

    private val _resumeState = MutableStateFlow(ResumeUiState(draft = repository.resumeDraft()))
    val resumeState: StateFlow<ResumeUiState> = _resumeState.asStateFlow()

    private val initialDuration = config.durationsMinutes.firstOrNull() ?: 20
    private val _interviewState = MutableStateFlow(
        InterviewUiState(
            styleId = config.styles.firstOrNull { it.id == "technical" }?.id
                ?: config.styles.firstOrNull()?.id
                ?: "technical",
            durationMinutes = initialDuration,
            remainingSeconds = initialDuration * 60,
        ),
    )
    val interviewState: StateFlow<InterviewUiState> = _interviewState.asStateFlow()

    val interviewConfig = config

    private var adviceJob: Job? = null
    private var resumeJob: Job? = null
    private var interviewJob: Job? = null
    private var remoteJob: Job? = null
    private var resumeLoadJob: Job? = null

    fun selectHuntingTab(tab: HuntingTab) {
        _huntingState.update { it.copy(selectedTab = tab, search = "") }
    }

    fun updateSearch(value: String) {
        _huntingState.update { it.copy(search = value) }
        loadMarketData(value)
    }

    fun updateAdviceForm(transform: (SeniorAdviceDraft) -> SeniorAdviceDraft) {
        _huntingState.update { state ->
            state.copy(
                form = state.form.copy(
                    draft = transform(state.form.draft),
                    error = null,
                    successMessage = null,
                ),
            )
        }
    }

    fun submitAdvice() {
        val form = _huntingState.value.form
        if (form.submitting) return
        val draft = form.draft
        val validationError = when {
            draft.company.isBlank() -> "请填写公司名称。"
            draft.position.isBlank() -> "请填写岗位名称。"
            draft.author.isBlank() -> "请填写分享人称呼。"
            draft.graduationYear.length != 4 ||
                draft.graduationYear.toIntOrNull() == null -> "毕业年份应为 4 位数字。"
            draft.advice.length < 10 -> "详细建议至少填写 10 个字符。"
            (draft.rating.toDoubleOrNull() ?: 0.0) !in 1.0..5.0 -> "评分应在 1 到 5 之间。"
            else -> null
        }
        if (validationError != null) {
            _huntingState.update { it.copy(form = it.form.copy(error = validationError)) }
            return
        }

        adviceJob?.cancel()
        adviceJob = viewModelScope.launch {
            _huntingState.update { it.copy(form = it.form.copy(submitting = true, error = null)) }
            delay(mockDelayMillis)
            repository.addSeniorAdvice(draft)
            _huntingState.update {
                it.copy(
                    advice = repository.seniorAdvice(),
                    form = AdviceFormUiState(
                        draft = SeniorAdviceDraft(),
                        successMessage = "经验已保存到本地 Mock 列表。",
                    ),
                )
            }
        }
    }

    fun resetAdviceForm() {
        adviceJob?.cancel()
        _huntingState.update { it.copy(form = AdviceFormUiState()) }
    }

    fun selectResumePane(pane: ResumePane) {
        _resumeState.update { it.copy(pane = pane) }
    }

    fun selectResumeSection(section: ResumeSection) {
        _resumeState.update { it.copy(section = section, pane = ResumePane.EDIT) }
    }

    fun updateResumeBasic(
        name: String? = null,
        targetPosition: String? = null,
        phone: String? = null,
        email: String? = null,
        address: String? = null,
    ) {
        updateResume { draft ->
            draft.copy(
                name = name ?: draft.name,
                targetPosition = targetPosition ?: draft.targetPosition,
                phone = phone ?: draft.phone,
                email = email ?: draft.email,
                address = address ?: draft.address,
            )
        }
    }

    fun updateSelfEvaluation(value: String) = updateResume { it.copy(selfEvaluation = value) }

    fun updateSkills(value: String) = updateResume { it.copy(skills = value) }

    fun addEducation() = updateResume { it.copy(education = it.education + ResumeEducation()) }

    fun updateEducation(index: Int, item: ResumeEducation) = updateResume { draft ->
        draft.copy(education = draft.education.replaceAt(index, item))
    }

    fun deleteEducation(index: Int) = updateResume { draft ->
        draft.copy(education = draft.education.withoutAt(index))
    }

    fun addExperience() = updateResume { it.copy(experience = it.experience + ResumeExperience()) }

    fun updateExperience(index: Int, item: ResumeExperience) = updateResume { draft ->
        draft.copy(experience = draft.experience.replaceAt(index, item))
    }

    fun deleteExperience(index: Int) = updateResume { draft ->
        draft.copy(experience = draft.experience.withoutAt(index))
    }

    fun addProject() = updateResume { it.copy(projects = it.projects + ResumeProject()) }

    fun updateProject(index: Int, item: ResumeProject) = updateResume { draft ->
        draft.copy(projects = draft.projects.replaceAt(index, item))
    }

    fun deleteProject(index: Int) = updateResume { draft ->
        draft.copy(projects = draft.projects.withoutAt(index))
    }

    fun addCertificate() = updateResume { it.copy(certificates = it.certificates + ResumeCertificate()) }

    fun updateCertificate(index: Int, item: ResumeCertificate) = updateResume { draft ->
        draft.copy(certificates = draft.certificates.replaceAt(index, item))
    }

    fun deleteCertificate(index: Int) = updateResume { draft ->
        draft.copy(certificates = draft.certificates.withoutAt(index))
    }

    fun saveResume() {
        if (_resumeState.value.saving) return
        resumeJob?.cancel()
        resumeJob = viewModelScope.launch {
            _resumeState.update { it.copy(saving = true, notice = null) }
            when (val result = repository.saveResumeRemote(_resumeState.value.draft)) {
                is AppResult.Success -> {
                    _resumeState.update {
                        it.copy(
                            draft = result.value,
                            saving = false,
                            notice = "简历已保存到服务端。",
                            noticeIsError = false,
                        )
                    }
                }
                is AppResult.Failure -> {
                    _resumeState.update {
                        it.copy(
                            saving = false,
                            notice = if (result.error.httpStatus == 501) {
                                "简历保存接口尚未实现，当前编辑内容仍保留在本地。"
                            } else {
                                result.error.message
                            },
                            noticeIsError = true,
                        )
                    }
                }
            }
        }
    }

    fun exportResume() {
        _resumeState.update {
            it.copy(
                notice = "已模拟导出 PDF；本阶段不会生成或分享真实文件。",
                noticeIsError = false,
            )
        }
    }

    fun clearResumeNotice() {
        _resumeState.update { it.copy(notice = null) }
    }

    fun selectInterviewStyle(styleId: String) {
        if (_interviewState.value.isRunning) return
        _interviewState.update { it.copy(styleId = styleId) }
    }

    fun selectInterviewDuration(minutes: Int) {
        if (_interviewState.value.isRunning) return
        _interviewState.update {
            it.copy(
                durationMinutes = minutes,
                remainingSeconds = minutes * 60,
            )
        }
    }

    fun toggleVoiceMock() {
        _interviewState.update { it.copy(voiceMockEnabled = !it.voiceMockEnabled) }
    }

    fun startInterview() {
        val current = _interviewState.value
        if (current.isRunning) return
        interviewJob?.cancel()
        val messageId = "interviewer-${UUID.randomUUID()}"
        _interviewState.update {
            it.copy(
                isRunning = true,
                isPaused = false,
                isLoading = true,
                remainingSeconds = it.durationMinutes * 60,
                messages = emptyList(),
                review = null,
                reviewLoading = false,
                sessionId = "",
                error = null,
                reviewProgress = "",
            )
        }
        interviewJob = viewModelScope.launch {
            val style = _interviewState.value.styleId
            runInterviewStream(
                request = InterviewChatRequest(
                    message = interviewStartPrompt(
                        style = style,
                        durationMinutes = _interviewState.value.durationMinutes,
                    ),
                    style = style,
                    enableTts = false,
                ),
                interviewerMessageId = messageId,
            )
        }
    }

    fun sendInterviewAnswer(answer: String) {
        val current = _interviewState.value
        val content = answer.trim()
        if (content.isBlank() || current.isLoading || current.isPaused || !current.isRunning) return
        if (current.sessionId.isBlank()) {
            _interviewState.update { it.copy(error = "尚未取得面试会话 ID，请重新开始面试。") }
            return
        }
        val userMessage = InterviewMessage(
            id = "user-${UUID.randomUUID()}",
            speaker = InterviewSpeaker.USER,
            content = content,
        )
        val messageId = "interviewer-${UUID.randomUUID()}"
        _interviewState.update {
            it.copy(messages = it.messages + userMessage, isLoading = true, error = null)
        }
        interviewJob?.cancel()
        interviewJob = viewModelScope.launch {
            runInterviewStream(
                request = InterviewChatRequest(
                    message = interviewAnswerPrompt(content, _interviewState.value.styleId),
                    style = _interviewState.value.styleId,
                    sessionId = _interviewState.value.sessionId,
                    enableTts = false,
                ),
                interviewerMessageId = messageId,
            )
        }
    }

    fun pauseInterview() {
        val current = _interviewState.value
        if (!current.isRunning) return
        if (!current.isPaused) {
            interviewJob?.cancel()
            interviewJob = null
        }
        _interviewState.update {
            it.copy(
                isPaused = !it.isPaused,
                isLoading = if (it.isPaused) false else it.isLoading,
            )
        }
    }

    fun tickInterviewClock() {
        val current = _interviewState.value
        if (!current.isRunning || current.isPaused || current.reviewLoading) return
        if (current.remainingSeconds <= 1) {
            finishInterview()
        } else {
            _interviewState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
        }
    }

    fun finishInterview() {
        val current = _interviewState.value
        if (!current.isRunning && current.review != null) return
        interviewJob?.cancel()
        val conversation = current.messages.joinToString("\n") { message ->
            val speaker = if (message.speaker == InterviewSpeaker.INTERVIEWER) "面试官" else "面试者"
            "$speaker：${message.content}"
        }
        if (conversation.length < 50) {
            _interviewState.update {
                it.copy(
                    isRunning = false,
                    isPaused = false,
                    isLoading = false,
                    reviewLoading = false,
                    error = "面试对话内容不足，无法生成复盘。",
                )
            }
            return
        }
        if (current.sessionId.isBlank()) {
            _interviewState.update {
                it.copy(
                    isRunning = false,
                    isPaused = false,
                    isLoading = false,
                    reviewLoading = false,
                    error = "缺少面试会话 ID，无法生成复盘。",
                )
            }
            return
        }
        _interviewState.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                isLoading = false,
                reviewLoading = true,
                error = null,
                reviewProgress = "",
            )
        }
        interviewJob = viewModelScope.launch {
            when (
                val result = repository.streamInterviewChat(
                    InterviewChatRequest(
                        message = interviewReviewPrompt(conversation, current.styleId),
                        mode = "interview_review",
                        style = current.styleId,
                        sessionId = current.sessionId,
                        enableTts = false,
                    ),
                ) { event ->
                    viewModelScope.launch {
                        if (event is InterviewStreamEvent.Chunk) {
                            _interviewState.update {
                                it.copy(reviewProgress = it.reviewProgress + event.content)
                            }
                        }
                    }
                }
            ) {
                is AppResult.Success -> {
                    val content = result.value.content.ifBlank {
                        _interviewState.value.reviewProgress
                    }
                    _interviewState.update {
                        it.copy(
                            reviewLoading = false,
                            review = content.takeIf(String::isNotBlank)?.let { review ->
                                InterviewReview(
                                    id = result.value.sessionId ?: it.sessionId,
                                    style = it.styleId,
                                    score = 0,
                                    summary = review,
                                    strengths = emptyList(),
                                    improvements = emptyList(),
                                    nextActions = emptyList(),
                                )
                            },
                            error = if (content.isBlank()) "未获取到复盘内容。" else null,
                        )
                    }
                }
                is AppResult.Failure -> {
                    _interviewState.update {
                        it.copy(
                            reviewLoading = false,
                            error = result.error.message,
                        )
                    }
                }
            }
        }
    }

    fun resetInterview() {
        interviewJob?.cancel()
        _interviewState.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                isLoading = false,
                messages = emptyList(),
                review = null,
                reviewLoading = false,
                sessionId = "",
                error = null,
                reviewProgress = "",
                remainingSeconds = it.durationMinutes * 60,
            )
        }
    }

    fun resetUserState() {
        adviceJob?.cancel()
        resumeJob?.cancel()
        interviewJob?.cancel()
        adviceJob = null
        resumeJob = null
        interviewJob = null
        remoteJob?.cancel()
        remoteJob = null
        resumeLoadJob?.cancel()
        resumeLoadJob = null
        _huntingState.value = HuntingUiState(
            jobs = repository.jobs(),
            positiveRankings = repository.companyRankings(true),
            negativeRankings = repository.companyRankings(false),
        )
        _resumeState.value = ResumeUiState(draft = ResumeDraft())
        _interviewState.value = InterviewUiState(
            styleId = config.styles.firstOrNull { it.id == "technical" }?.id
                ?: config.styles.firstOrNull()?.id
                ?: "technical",
            durationMinutes = initialDuration,
            remainingSeconds = initialDuration * 60,
        )
    }

    fun loadRemoteData(search: String = _huntingState.value.search) {
        loadMarketData(search)
        resumeLoadJob?.cancel()
        resumeLoadJob = viewModelScope.launch {
            when (val result = repository.loadResume()) {
                is AppResult.Success -> {
                    _resumeState.update {
                        it.copy(
                            draft = result.value ?: ResumeDraft(),
                            notice = if (result.value == null) {
                                "尚未创建简历，首次保存时将在服务端创建。"
                            } else {
                                null
                            },
                            noticeIsError = false,
                        )
                    }
                }
                is AppResult.Failure -> {
                    _resumeState.update {
                        it.copy(notice = result.error.message, noticeIsError = true)
                    }
                }
            }
        }
    }

    private fun loadMarketData(search: String) {
        remoteJob?.cancel()
        remoteJob = viewModelScope.launch {
            _huntingState.update { it.copy(loading = true, error = null) }
            val jobs = repository.loadJobs(search, page = 1, pageSize = 100)
            val advice = repository.loadSeniorAdvice(
                search = search,
                ordering = "recent",
                page = 1,
                pageSize = 50,
            )
            val errors = listOfNotNull(
                (jobs as? AppResult.Failure)?.error?.message,
                (advice as? AppResult.Failure)?.error?.message,
            )
            _huntingState.update {
                it.copy(
                    jobs = (jobs as? AppResult.Success)?.value?.items ?: it.jobs,
                    advice = (advice as? AppResult.Success)?.value?.items ?: it.advice,
                    loading = false,
                    error = errors.firstOrNull(),
                )
            }
        }
    }

    private suspend fun runInterviewStream(
        request: InterviewChatRequest,
        interviewerMessageId: String,
    ) {
        when (
            val result = repository.streamInterviewChat(request) { event ->
                viewModelScope.launch {
                    when (event) {
                        is InterviewStreamEvent.Chunk ->
                            appendInterviewerChunk(interviewerMessageId, event.content)
                        is InterviewStreamEvent.Session ->
                            _interviewState.update { it.copy(sessionId = event.sessionId) }
                        is InterviewStreamEvent.ServerError ->
                            _interviewState.update { it.copy(error = event.message) }
                        is InterviewStreamEvent.Audio,
                        InterviewStreamEvent.Done -> Unit
                    }
                }
            }
        ) {
            is AppResult.Success -> {
                _interviewState.update {
                    it.copy(
                        isLoading = false,
                        sessionId = result.value.sessionId ?: it.sessionId,
                        error = null,
                    )
                }
            }
            is AppResult.Failure -> {
                _interviewState.update {
                    it.copy(
                        isLoading = false,
                        error = result.error.message,
                    )
                }
            }
        }
    }

    private fun appendInterviewerChunk(messageId: String, delta: String) {
        if (delta.isEmpty()) return
        _interviewState.update { state ->
            val existing = state.messages.indexOfFirst { it.id == messageId }
            if (existing >= 0) {
                val messages = state.messages.toMutableList()
                val message = messages[existing]
                messages[existing] = message.copy(content = message.content + delta)
                state.copy(messages = messages)
            } else {
                state.copy(
                    messages = state.messages + InterviewMessage(
                        id = messageId,
                        speaker = InterviewSpeaker.INTERVIEWER,
                        content = delta,
                    ),
                )
            }
        }
    }

    private fun interviewStartPrompt(style: String, durationMinutes: Int): String {
        val resume = formatResumePrompt(_resumeState.value.draft)
        return buildString {
            append(
                "【面试模式】你是一位${interviewStyleDescription(style)}面试官。" +
                    "请开始一场${durationMinutes}分钟的面试。",
            )
            if (resume.isNotBlank()) {
                append("\n\n【面试者简历信息】\n")
                append(resume)
                append("\n\n请根据以上简历信息，提出针对性的面试问题。")
            }
            append(
                "\n\n要求：\n" +
                    "1. 首先进行简短的自我介绍，说明你是面试官和面试风格\n" +
                    "2. 然后提出第一个面试问题\n" +
                    "3. 全程只扮演面试官\n" +
                    "4. 根据回答提出下一个相关问题\n" +
                    "5. 保持${interviewStyleDescription(style)}的风格\n\n现在开始面试。",
            )
        }
    }

    private fun interviewAnswerPrompt(answer: String, style: String): String =
        "【面试模式】用户的回答是：\"$answer\"\n\n" +
            "作为${interviewStyleDescription(style)}面试官，请：\n" +
            "1. 简要评价用户的回答（可选）\n" +
            "2. 提出下一个面试问题\n\n" +
            "你只需扮演面试官角色，不要查询任何数据库信息。"

    private fun interviewReviewPrompt(conversation: String, style: String): String =
        "【面试复盘模式】请对以下面试过程进行复盘，并提供改进建议。\n\n" +
            "面试风格：${interviewStyleDescription(style)}\n\n" +
            "对话记录：\n$conversation\n\n" +
            "请提供：\n1. 面试表现总结\n2. 回答优点\n3. 需要改进的地方\n4. 具体改进建议\n5. 下次准备建议"

    private fun formatResumePrompt(resume: ResumeDraft): String = buildString {
        if (resume.name.isNotBlank()) append("姓名：${resume.name}\n")
        if (resume.targetPosition.isNotBlank()) append("意向岗位：${resume.targetPosition}\n")
        if (resume.selfEvaluation.isNotBlank()) append("个人自评：${resume.selfEvaluation}\n")
        if (resume.skills.isNotBlank()) append("专业技能：${resume.skills}\n")
        if (resume.education.isNotEmpty()) {
            append("教育背景：\n")
            resume.education.filter { it.school.isNotBlank() }.forEach {
                append("- ${it.school} ${it.major} ${it.degree} ${it.period}\n")
            }
        }
        if (resume.experience.isNotEmpty()) {
            append("实习经历：\n")
            resume.experience.filter { it.company.isNotBlank() }.forEach {
                append("- ${it.company} ${it.position} ${it.period}\n")
                if (it.description.isNotBlank()) append("  ${it.description}\n")
            }
        }
        if (resume.projects.isNotEmpty()) {
            append("项目经验：\n")
            resume.projects.filter { it.name.isNotBlank() }.forEach {
                append("- ${it.name} ${it.role} ${it.period}\n")
                if (it.description.isNotBlank()) append("  ${it.description}\n")
            }
        }
        if (resume.certificates.isNotEmpty()) {
            append("证书认证：\n")
            resume.certificates.filter { it.name.isNotBlank() }.forEach {
                append("- ${it.name} ${it.issuer} ${it.period}\n")
            }
        }
    }.trim()

    private fun interviewStyleDescription(style: String): String = when (style) {
        "gentle" -> "温和友好的"
        "pressure" -> "压力型"
        "technical" -> "技术导向的"
        "behavioral" -> "行为面试"
        else -> "专业的"
    }

    private fun updateResume(transform: (ResumeDraft) -> ResumeDraft) {
        _resumeState.update {
            it.copy(
                draft = transform(it.draft),
                notice = null,
                noticeIsError = false,
            )
        }
    }
}

class CareerPrepViewModelFactory(
    private val repository: AppRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(CareerPrepViewModel::class.java))
        return CareerPrepViewModel(repository) as T
    }
}

private fun <T> List<T>.replaceAt(index: Int, value: T): List<T> =
    mapIndexed { itemIndex, item -> if (itemIndex == index) value else item }

private fun <T> List<T>.withoutAt(index: Int): List<T> =
    filterIndexed { itemIndex, _ -> itemIndex != index }
