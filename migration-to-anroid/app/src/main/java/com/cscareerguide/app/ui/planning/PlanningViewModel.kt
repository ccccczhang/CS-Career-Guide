package com.cscareerguide.app.ui.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cscareerguide.app.data.AppRepository
import com.cscareerguide.app.data.model.CareerCategory
import com.cscareerguide.app.data.model.CareerPathSummary
import com.cscareerguide.app.data.model.CareerPlanReport
import com.cscareerguide.app.data.model.IntroductionDraft
import com.cscareerguide.app.data.model.Recommendation
import com.cscareerguide.app.data.model.SkillGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DraftSaveStatus {
    IDLE,
    PENDING,
    SAVED,
    UNAVAILABLE,
}

data class PlanningUiState(
    val careerPaths: List<CareerPathSummary> = emptyList(),
    val skillGroups: List<SkillGroup> = emptyList(),
    val careerCategories: List<CareerCategory> = emptyList(),
    val introduction: IntroductionDraft = IntroductionDraft(),
    val draftStatus: DraftSaveStatus = DraftSaveStatus.IDLE,
    val introductionError: String? = null,
    val recommendations: List<Recommendation> = emptyList(),
    val selectedCareer: String? = null,
    val report: CareerPlanReport? = null,
) {
    val hasIntroduction: Boolean
        get() = introduction.name.isNotBlank() &&
            introduction.school.isNotBlank() &&
            introduction.major.isNotBlank() &&
            introduction.grade.isNotBlank() &&
            introduction.goal.isNotBlank() &&
            introduction.skills.isNotEmpty()
}

class PlanningViewModel(
    private val repository: AppRepository,
    private val draftSaveDelayMillis: Long = 600,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        PlanningUiState(
            careerPaths = repository.careerPaths(),
            skillGroups = repository.skillGroups(),
            careerCategories = repository.careerCategories(),
            introduction = repository.introductionDraft(),
        ),
    )
    val uiState: StateFlow<PlanningUiState> = _uiState.asStateFlow()

    private var saveJob: Job? = null

    fun updateIntroduction(transform: (IntroductionDraft) -> IntroductionDraft) {
        _uiState.update {
            it.copy(
                introduction = transform(it.introduction),
                draftStatus = DraftSaveStatus.PENDING,
                introductionError = null,
            )
        }
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(draftSaveDelayMillis)
            saveDraftNow()
        }
    }

    fun toggleSkill(value: String) {
        updateIntroduction { draft ->
            draft.copy(
                skills = if (value in draft.skills) draft.skills - value else draft.skills + value,
            )
        }
    }

    fun saveDraftNow() {
        saveJob?.cancel()
        saveJob = null
        try {
            repository.saveIntroduction(_uiState.value.introduction)
            _uiState.update { it.copy(draftStatus = DraftSaveStatus.SAVED) }
        } catch (_: Exception) {
            _uiState.update { it.copy(draftStatus = DraftSaveStatus.UNAVAILABLE) }
        }
    }

    fun finishIntroduction(): Boolean {
        val state = _uiState.value
        if (!state.hasIntroduction) {
            _uiState.update {
                it.copy(introductionError = "请先填写姓名、学校、专业、年级、目标职业，并至少选择一项技能。")
            }
            return false
        }
        saveDraftNow()
        _uiState.update {
            it.copy(
                recommendations = repository.recommendations(it.introduction),
                introductionError = null,
            )
        }
        return true
    }

    fun ensureRecommendations() {
        _uiState.update { state ->
            if (state.recommendations.isNotEmpty()) {
                state
            } else {
                state.copy(recommendations = repository.recommendations(state.introduction))
            }
        }
    }

    fun selectCareer(career: String?) {
        _uiState.update {
            it.copy(
                selectedCareer = career,
                report = null,
            )
        }
    }

    fun createReport(): Boolean {
        val state = _uiState.value
        val career = state.selectedCareer
        if (career.isNullOrBlank()) {
            _uiState.update {
                it.copy(introductionError = "请先选择一个目标岗位。")
            }
            return false
        }
        val report = repository.careerPlan(state.introduction, career)
        _uiState.update {
            it.copy(
                report = report,
                introductionError = null,
            )
        }
        return true
    }

    fun clearError() {
        _uiState.update { it.copy(introductionError = null) }
    }

    fun resetUserState() {
        saveJob?.cancel()
        saveJob = null
        _uiState.value = PlanningUiState(
            careerPaths = repository.careerPaths(),
            skillGroups = repository.skillGroups(),
            careerCategories = repository.careerCategories(),
        )
    }

    override fun onCleared() {
        if (_uiState.value.draftStatus == DraftSaveStatus.PENDING) {
            saveDraftNow()
        }
        super.onCleared()
    }
}

class PlanningViewModelFactory(
    private val repository: AppRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PlanningViewModel::class.java))
        return PlanningViewModel(repository) as T
    }
}
