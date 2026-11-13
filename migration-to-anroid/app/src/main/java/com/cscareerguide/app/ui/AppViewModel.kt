package com.cscareerguide.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cscareerguide.app.data.AppRepository
import com.cscareerguide.app.data.AuthOutcome
import com.cscareerguide.app.data.model.CareerPathSummary
import com.cscareerguide.app.data.model.HomeMetrics
import com.cscareerguide.app.data.model.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
    val user: UserProfile? = null,
    val careerPaths: List<CareerPathSummary> = emptyList(),
    val homeMetrics: HomeMetrics = HomeMetrics(0, 0),
    val authLoading: Boolean = false,
    val authError: String? = null,
    val profileSaving: Boolean = false,
    val profileError: String? = null,
) {
    val isAuthenticated: Boolean
        get() = user != null
}

class AppViewModel(
    private val repository: AppRepository,
    private val authDelayMillis: Long = 350L,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AppUiState(
            user = repository.currentUser(),
            careerPaths = repository.careerPaths(),
            homeMetrics = repository.homeMetrics(),
        ),
    )
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var authJob: Job? = null
    private var profileJob: Job? = null

    init {
        viewModelScope.launch {
            repository.authInvalidations.collect {
                _uiState.update {
                    it.copy(
                        user = null,
                        authLoading = false,
                        profileSaving = false,
                        profileError = null,
                    )
                }
            }
        }
        if (repository.currentUser() != null) {
            _uiState.update { it.copy(authLoading = true) }
            viewModelScope.launch {
                when (val result = repository.restoreSession()) {
                    is AuthOutcome.Success -> {
                        _uiState.update {
                            it.copy(
                                user = result.user,
                                authLoading = false,
                                authError = null,
                            )
                        }
                    }
                    is AuthOutcome.Failure -> {
                        _uiState.update {
                            it.copy(
                                user = null,
                                authLoading = false,
                                authError = result.message.takeIf {
                                    result.error?.authExpired != true
                                },
                            )
                        }
                    }
                    null -> {
                        _uiState.update { it.copy(authLoading = false) }
                    }
                }
            }
        }
    }

    fun login(username: String, password: String) {
        runAuth { repository.login(username, password) }
    }

    fun register(username: String, password: String, confirmation: String) {
        runAuth { repository.register(username, password, confirmation) }
    }

    fun logout() {
        authJob?.cancel()
        profileJob?.cancel()
        _uiState.update {
            it.copy(
                user = null,
                authLoading = false,
                authError = null,
                profileSaving = false,
                profileError = null,
            )
        }
        viewModelScope.launch { repository.logout() }
    }

    fun clearAuthError() {
        _uiState.update { it.copy(authError = null) }
    }

    fun updateProfile(
        displayName: String,
        school: String,
        major: String,
        grade: String,
        education: String,
        email: String,
        address: String,
        profile: String,
    ) {
        val current = _uiState.value.user ?: return
        val updated = current.copy(
            displayName = displayName.trim().ifBlank { current.displayName },
            school = school.trim(),
            major = major.trim(),
            grade = grade.trim(),
            education = education.trim(),
            email = email.trim(),
            address = address.trim(),
            profile = profile.trim(),
        )
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            _uiState.update { it.copy(profileSaving = true, profileError = null) }
            when (val result = repository.updateProfile(updated)) {
                is AuthOutcome.Success -> {
                    _uiState.update {
                        it.copy(
                            user = result.user,
                            profileSaving = false,
                            profileError = null,
                        )
                    }
                }
                is AuthOutcome.Failure -> {
                    _uiState.update {
                        it.copy(
                            user = if (result.error?.authExpired == true) null else it.user,
                            profileSaving = false,
                            profileError = result.message,
                        )
                    }
                }
            }
        }
    }

    private fun runAuth(action: suspend () -> AuthOutcome) {
        if (_uiState.value.authLoading) return
        authJob?.cancel()
        authJob = viewModelScope.launch {
            _uiState.update { it.copy(authLoading = true, authError = null) }
            delay(authDelayMillis)
            when (val result = action()) {
                is AuthOutcome.Success -> {
                    _uiState.update {
                        it.copy(
                            user = result.user,
                            authLoading = false,
                            authError = null,
                        )
                    }
                }

                is AuthOutcome.Failure -> {
                    _uiState.update {
                        it.copy(
                            authLoading = false,
                            authError = result.message,
                        )
                    }
                }
            }
        }
    }
}

class AppViewModelFactory(
    private val repository: AppRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AppViewModel::class.java))
        return AppViewModel(repository) as T
    }
}
