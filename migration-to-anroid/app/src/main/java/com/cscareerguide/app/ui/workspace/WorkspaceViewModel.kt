package com.cscareerguide.app.ui.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cscareerguide.app.data.AppRepository
import com.cscareerguide.app.data.model.CatalogItem
import com.cscareerguide.app.data.model.CatalogKind
import com.cscareerguide.app.data.model.EvaluationRun
import com.cscareerguide.app.data.model.PracticeQuestion
import com.cscareerguide.app.data.model.PracticeRecordDetail
import com.cscareerguide.app.data.model.ResumeAnalysis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val kind: CatalogKind = CatalogKind.JOBS,
    val query: String = "",
    val city: String = "",
    val role: String = "",
    val topic: String = "",
    val difficulty: String = "",
    val pageSize: Int = 10,
    val items: List<CatalogItem> = emptyList(),
    val total: Int = 0,
    val latestSource: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

data class ResumeOptimizationUiState(
    val jdText: String = "",
    val resumeRevision: Int = 3,
    val analysis: ResumeAnalysis? = null,
    val acceptedSuggestionIds: Set<String> = emptySet(),
    val busy: Boolean = false,
    val status: String = "填写目标 JD 后生成本地 Mock 分析。",
    val isError: Boolean = false,
)

data class PracticeUiState(
    val role: String = "Java后端",
    val topic: String = "集合",
    val difficulty: String = "easy",
    val query: String = "集合",
    val topK: Int = 5,
    val submitted: Boolean = false,
    val results: List<PracticeQuestion> = emptyList(),
    val loading: Boolean = false,
    val status: String = "选择条件后开始检索。",
    val isError: Boolean = false,
)

data class RecordsUiState(
    val query: String = "",
    val role: String = "",
    val topic: String = "",
    val difficulty: String = "",
    val records: List<PracticeRecordDetail> = emptyList(),
    val selectedId: String? = null,
    val loading: Boolean = false,
    val status: String = "正在读取本地练习记录。",
) {
    val selected: PracticeRecordDetail?
        get() = records.firstOrNull { it.id == selectedId }
}

data class EvaluationUiState(
    val datasetVersion: String = "java-eval-test-v1",
    val strategy: String = "text-baseline",
    val topK: String = "5",
    val runs: List<EvaluationRun> = emptyList(),
    val selected: EvaluationRun? = null,
    val loading: Boolean = false,
    val status: String = "当前展示本地 Mock 评估记录。",
    val isError: Boolean = false,
)

data class WorkspaceUiState(
    val catalog: CatalogUiState = CatalogUiState(),
    val resumeOptimization: ResumeOptimizationUiState = ResumeOptimizationUiState(),
    val practice: PracticeUiState = PracticeUiState(),
    val records: RecordsUiState = RecordsUiState(),
    val evaluation: EvaluationUiState = EvaluationUiState(),
)

class WorkspaceViewModel(
    private val repository: AppRepository,
    private val mockDelayMillis: Long = 350L,
) : ViewModel() {
    private val allCatalog = repository.catalogItems()
    private val allPracticeQuestions = repository.practiceQuestions()
    private val allRecords = repository.practiceRecords()
    private val initialRuns = repository.evaluationRuns()

    private val _uiState = MutableStateFlow(
        WorkspaceUiState(
            catalog = CatalogUiState(
                items = allCatalog.filter { it.kind == CatalogKind.JOBS },
                total = allCatalog.count { it.kind == CatalogKind.JOBS },
                latestSource = allCatalog.firstOrNull { it.kind == CatalogKind.JOBS }?.source.orEmpty(),
            ),
            records = RecordsUiState(
                records = allRecords,
                selectedId = allRecords.firstOrNull()?.id,
            ),
            evaluation = EvaluationUiState(
                runs = initialRuns,
                selected = initialRuns.firstOrNull(),
            ),
        ),
    )
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    private var catalogJob: Job? = null
    private var resumeJob: Job? = null
    private var practiceJob: Job? = null
    private var evaluationJob: Job? = null

    fun selectCatalogKind(kind: CatalogKind) {
        _uiState.update {
            it.copy(
                catalog = it.catalog.copy(
                    kind = kind,
                    query = "",
                    city = "",
                    role = "",
                    topic = "",
                    difficulty = "",
                    error = null,
                ),
            )
        }
        searchCatalog()
    }

    fun updateCatalogFilters(
        query: String? = null,
        city: String? = null,
        role: String? = null,
        topic: String? = null,
        difficulty: String? = null,
    ) {
        _uiState.update {
            it.copy(
                catalog = it.catalog.copy(
                    query = query ?: it.catalog.query,
                    city = city ?: it.catalog.city,
                    role = role ?: it.catalog.role,
                    topic = topic ?: it.catalog.topic,
                    difficulty = difficulty ?: it.catalog.difficulty,
                    error = null,
                ),
            )
        }
    }

    fun searchCatalog() {
        catalogJob?.cancel()
        catalogJob = viewModelScope.launch {
            _uiState.update { it.copy(catalog = it.catalog.copy(loading = true, error = null)) }
            delay(mockDelayMillis)
            val state = _uiState.value.catalog
            val filtered = allCatalog.filter { item ->
                item.kind == state.kind &&
                    matches(state.query, item.title, item.subtitle, item.description, item.source) &&
                    matches(state.city, item.city) &&
                    matches(state.role, item.role, item.subtitle, item.tags.joinToString()) &&
                    matches(state.topic, item.topic, item.title) &&
                    (state.difficulty.isBlank() || item.difficulty == state.difficulty)
            }
            _uiState.update {
                it.copy(
                    catalog = it.catalog.copy(
                        items = filtered,
                        total = filtered.size,
                        latestSource = filtered.firstOrNull()?.source.orEmpty(),
                        loading = false,
                    ),
                )
            }
        }
    }

    fun resetCatalogFilters() {
        updateCatalogFilters("", "", "", "", "")
        searchCatalog()
    }

    fun updateJdText(value: String) {
        _uiState.update {
            it.copy(
                resumeOptimization = it.resumeOptimization.copy(
                    jdText = value,
                    status = "填写目标 JD 后生成本地 Mock 分析。",
                    isError = false,
                ),
            )
        }
    }

    fun updateResumeRevision(value: String) {
        _uiState.update {
            it.copy(
                resumeOptimization = it.resumeOptimization.copy(
                    resumeRevision = value.filter(Char::isDigit).take(6).toIntOrNull() ?: 0,
                ),
            )
        }
    }

    fun analyzeResume() {
        val state = _uiState.value.resumeOptimization
        if (state.busy) return
        if (state.jdText.isBlank()) {
            _uiState.update {
                it.copy(
                    resumeOptimization = it.resumeOptimization.copy(
                        status = "请先填写目标岗位 JD。",
                        isError = true,
                    ),
                )
            }
            return
        }
        resumeJob?.cancel()
        resumeJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    resumeOptimization = it.resumeOptimization.copy(
                        busy = true,
                        status = "正在生成 Mock 分析…",
                        isError = false,
                    ),
                )
            }
            delay(mockDelayMillis)
            val analysis = repository.resumeAnalysis(_uiState.value.resumeOptimization.resumeRevision)
            _uiState.update {
                it.copy(
                    resumeOptimization = it.resumeOptimization.copy(
                        analysis = analysis,
                        acceptedSuggestionIds = emptySet(),
                        busy = false,
                        status = analysis.summary,
                    ),
                )
            }
        }
    }

    fun toggleResumeSuggestion(id: String) {
        _uiState.update {
            val accepted = it.resumeOptimization.acceptedSuggestionIds
            it.copy(
                resumeOptimization = it.resumeOptimization.copy(
                    acceptedSuggestionIds = if (id in accepted) accepted - id else accepted + id,
                    status = if (id in accepted) "已取消该建议。" else "已选中该建议，确认前不会修改简历。",
                    isError = false,
                ),
            )
        }
    }

    fun applyResumeSuggestions() {
        val state = _uiState.value.resumeOptimization
        if (state.acceptedSuggestionIds.isEmpty()) {
            _uiState.update {
                it.copy(
                    resumeOptimization = it.resumeOptimization.copy(
                        status = "请至少选择一条修改建议。",
                        isError = true,
                    ),
                )
            }
            return
        }
        _uiState.update {
            it.copy(
                resumeOptimization = it.resumeOptimization.copy(
                    status = "已模拟确认 ${state.acceptedSuggestionIds.size} 条建议；真实应用需等待桥接接口。",
                    isError = false,
                ),
            )
        }
    }

    fun updatePracticeFilters(
        role: String? = null,
        topic: String? = null,
        difficulty: String? = null,
        query: String? = null,
        topK: Int? = null,
    ) {
        _uiState.update {
            it.copy(
                practice = it.practice.copy(
                    role = role ?: it.practice.role,
                    topic = topic ?: it.practice.topic,
                    difficulty = difficulty ?: it.practice.difficulty,
                    query = query ?: it.practice.query,
                    topK = topK ?: it.practice.topK,
                    submitted = false,
                    results = emptyList(),
                ),
            )
        }
    }

    fun applyPracticePreset(kind: String) {
        when (kind) {
            "rank" -> updatePracticeFilters(
                role = "Java后端",
                topic = "集合",
                difficulty = "easy",
                query = "ArrayList",
                topK = 5,
            )
            "empty" -> updatePracticeFilters(
                role = "Java后端",
                topic = "不存在的知识点",
                difficulty = "easy",
                query = "不存在",
                topK = 5,
            )
            else -> updatePracticeFilters(
                role = "Java后端",
                topic = "集合",
                difficulty = "easy",
                query = "集合",
                topK = 5,
            )
        }
        retrievePracticeQuestions()
    }

    fun retrievePracticeQuestions() {
        val state = _uiState.value.practice
        if (state.loading) return
        practiceJob?.cancel()
        practiceJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    practice = it.practice.copy(
                        loading = true,
                        submitted = true,
                        status = "正在筛选并排序候选题目…",
                        isError = false,
                    ),
                )
            }
            delay(mockDelayMillis)
            val results = allPracticeQuestions
                .filter { it.role == state.role }
                .filter { state.topic.isBlank() || it.topic.contains(state.topic, ignoreCase = true) }
                .filter { it.difficulty == state.difficulty }
                .filter {
                    state.query.isBlank() ||
                        it.question.contains(state.query, ignoreCase = true) ||
                        it.topic.contains(state.query, ignoreCase = true)
                }
                .take(state.topK.coerceIn(1, 20))
            _uiState.update {
                it.copy(
                    practice = it.practice.copy(
                        results = results,
                        loading = false,
                        status = if (results.isEmpty()) {
                            "没有符合条件的演示题。"
                        } else {
                            "检索完成，返回 ${results.size} 道有来源的演示题。"
                        },
                    ),
                )
            }
        }
    }

    fun updateRecordFilters(
        query: String? = null,
        role: String? = null,
        topic: String? = null,
        difficulty: String? = null,
    ) {
        _uiState.update {
            it.copy(
                records = it.records.copy(
                    query = query ?: it.records.query,
                    role = role ?: it.records.role,
                    topic = topic ?: it.records.topic,
                    difficulty = difficulty ?: it.records.difficulty,
                ),
            )
        }
    }

    fun searchRecords() {
        val state = _uiState.value.records
        val filtered = allRecords.filter {
            matches(state.query, it.title, it.role, it.topic) &&
                matches(state.role, it.role) &&
                matches(state.topic, it.topic) &&
                (state.difficulty.isBlank() || it.difficulty == state.difficulty)
        }
        _uiState.update {
            it.copy(
                records = it.records.copy(
                    records = filtered,
                    selectedId = filtered.firstOrNull()?.id,
                    status = if (filtered.isEmpty()) "没有符合条件的练习记录。" else "已加载 ${filtered.size} 条记录。",
                ),
            )
        }
    }

    fun selectRecord(id: String) {
        _uiState.update { it.copy(records = it.records.copy(selectedId = id)) }
    }

    fun updateEvaluationForm(
        datasetVersion: String? = null,
        strategy: String? = null,
        topK: String? = null,
    ) {
        _uiState.update {
            it.copy(
                evaluation = it.evaluation.copy(
                    datasetVersion = datasetVersion ?: it.evaluation.datasetVersion,
                    strategy = strategy ?: it.evaluation.strategy,
                    topK = topK?.filter(Char::isDigit)?.take(2) ?: it.evaluation.topK,
                    isError = false,
                ),
            )
        }
    }

    fun runEvaluation() {
        val state = _uiState.value.evaluation
        if (state.loading) return
        val topK = state.topK.toIntOrNull()
        if (topK == null || topK !in 1..20) {
            _uiState.update {
                it.copy(
                    evaluation = it.evaluation.copy(
                        status = "Top-K 必须是 1–20 的整数。",
                        isError = true,
                    ),
                )
            }
            return
        }
        evaluationJob?.cancel()
        evaluationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    evaluation = it.evaluation.copy(
                        loading = true,
                        status = "正在运行 Mock 文本策略…",
                        isError = false,
                    ),
                )
            }
            delay(mockDelayMillis)
            val result = repository.evaluationRun(state.datasetVersion, state.strategy, topK)
            _uiState.update {
                it.copy(
                    evaluation = it.evaluation.copy(
                        runs = listOf(result) + it.evaluation.runs,
                        selected = result,
                        loading = false,
                        status = "Mock 评估完成：${result.sampleCount} 条样本，失败 ${result.failureCount} 条。",
                    ),
                )
            }
        }
    }

    fun openEvaluation(id: String) {
        _uiState.update {
            val run = it.evaluation.runs.firstOrNull { item -> item.id == id }
            it.copy(
                evaluation = it.evaluation.copy(
                    selected = run,
                    status = run?.let { item -> "已读取评估记录 ${item.id}。" } ?: "评估记录不存在。",
                    isError = run == null,
                ),
            )
        }
    }

    fun exportEvaluation() {
        val selected = _uiState.value.evaluation.selected
        _uiState.update {
            it.copy(
                evaluation = it.evaluation.copy(
                    status = if (selected == null) {
                        "请先选择一条评估记录。"
                    } else {
                        "已模拟导出 ${selected.id} 的 JSON；真实文件导出需桥接。"
                    },
                    isError = selected == null,
                ),
            )
        }
    }

    private fun matches(query: String, vararg values: String): Boolean {
        if (query.isBlank()) return true
        return values.any { it.contains(query.trim(), ignoreCase = true) }
    }
}

class WorkspaceViewModelFactory(
    private val repository: AppRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(WorkspaceViewModel::class.java))
        return WorkspaceViewModel(repository) as T
    }
}
