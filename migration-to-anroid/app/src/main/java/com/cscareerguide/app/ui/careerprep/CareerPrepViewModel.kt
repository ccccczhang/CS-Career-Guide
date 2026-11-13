package com.cscareerguide.app.ui.careerprep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cscareerguide.app.data.AppRepository
import com.cscareerguide.app.data.AppResult
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
        _interviewState.update {
            it.copy(
                isRunning = true,
                isPaused = false,
                isLoading = true,
                remainingSeconds = it.durationMinutes * 60,
                messages = emptyList(),
                review = null,
                reviewLoading = false,
            )
        }
        interviewJob = viewModelScope.launch {
            delay(mockDelayMillis)
            appendInterviewerMessage(firstQuestion(_interviewState.value.styleId))
        }
    }

    fun sendInterviewAnswer(answer: String) {
        val current = _interviewState.value
        val content = answer.trim()
        if (content.isBlank() || current.isLoading || current.isPaused || !current.isRunning) return
        val userMessage = InterviewMessage(
            id = "message-${current.messages.size + 1}",
            speaker = InterviewSpeaker.USER,
            content = content,
        )
        _interviewState.update {
            it.copy(messages = it.messages + userMessage, isLoading = true)
        }
        interviewJob?.cancel()
        interviewJob = viewModelScope.launch {
            delay(mockDelayMillis)
            val followUp = when ((_interviewState.value.messages.size - 1) % 4) {
                1 -> "你提到了项目中的技术选择。请说明当时比较过哪些方案，以及为什么最终选择现在的实现。"
                2 -> "这个项目最终解决了什么具体问题？请给出一个可验证的结果或测试方式。"
                3 -> "如果流量增长十倍，原来的设计最可能先出现什么问题？你会怎样定位？"
                else -> "最后，用一分钟概括你的优势，并说明它与目标岗位的关系。"
            }
            appendInterviewerMessage(followUp)
        }
    }

    fun pauseInterview() {
        val current = _interviewState.value
        if (!current.isRunning) return
        _interviewState.update { it.copy(isPaused = !it.isPaused) }
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
        _interviewState.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                isLoading = false,
                reviewLoading = true,
            )
        }
        interviewJob = viewModelScope.launch {
            delay(mockDelayMillis)
            _interviewState.update {
                it.copy(
                    reviewLoading = false,
                    review = repository.interviewReview(it.styleId),
                )
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

    private fun appendInterviewerMessage(content: String) {
        _interviewState.update {
            it.copy(
                messages = it.messages + InterviewMessage(
                    id = "message-${it.messages.size + 1}",
                    speaker = InterviewSpeaker.INTERVIEWER,
                    content = content,
                ),
                isLoading = false,
            )
        }
    }

    private fun firstQuestion(styleId: String): String = when (styleId) {
        "gentle" -> "先不用紧张。请从最近一个项目开始，说明你负责了哪一部分。"
        "pressure" -> "请在一分钟内说明你的项目目标、关键难点和可验证结果。不要泛泛介绍。"
        "behavioral" -> "请讲一次你和同伴对方案有分歧的经历，重点说明你如何处理并推动结果。"
        else -> "请选择一个你最有代表性的项目，说明技术栈、你的职责和最难处理的问题。"
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
