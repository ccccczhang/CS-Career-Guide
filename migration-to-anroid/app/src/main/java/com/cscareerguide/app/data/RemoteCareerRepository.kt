package com.cscareerguide.app.data

import com.cscareerguide.app.data.bridge.AuthSession
import com.cscareerguide.app.data.bridge.BridgeError
import com.cscareerguide.app.data.bridge.BridgeResult
import com.cscareerguide.app.data.bridge.CatalogBridgeQuery
import com.cscareerguide.app.data.bridge.CareerBridge
import com.cscareerguide.app.data.bridge.PracticeRetrieveRequest
import com.cscareerguide.app.data.bridge.ProfileUpdate
import com.cscareerguide.app.data.bridge.ResumeAnalysisRequest
import com.cscareerguide.app.data.bridge.ResumeApplyRequest
import com.cscareerguide.app.data.model.CatalogItem
import com.cscareerguide.app.data.model.JobSummary
import com.cscareerguide.app.data.model.PracticeRetrieval
import com.cscareerguide.app.data.model.ResumeDraft
import com.cscareerguide.app.data.model.SeniorAdvice
import com.cscareerguide.app.data.model.UserProfile
import com.cscareerguide.app.data.session.AuthSessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RemoteCareerRepository(
    private val bridge: CareerBridge,
    private val sessionStore: AuthSessionStore,
    private val mock: InMemoryMockRepository = InMemoryMockRepository(),
) : AppRepository by mock {
    private val invalidations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val authInvalidations: Flow<Unit> = invalidations.asSharedFlow()

    override fun currentUser(): UserProfile? = sessionStore.user()

    override suspend fun restoreSession(): AuthOutcome? {
        if (sessionStore.token().isNullOrBlank()) return null
        return when (val result = bridge.profile()) {
            is BridgeResult.Success -> {
                sessionStore.save(
                    token = requireNotNull(sessionStore.token()),
                    user = result.value,
                )
                AuthOutcome.Success(result.value)
            }
            is BridgeResult.Failure -> failure(result.error)
            is BridgeResult.NotConfigured -> AuthOutcome.Failure(result.route)
        }
    }

    override suspend fun login(username: String, password: String): AuthOutcome {
        val normalizedUsername = username.trim()
        if (normalizedUsername.isBlank()) return AuthOutcome.Failure("请输入账号。")
        if (password.isBlank()) return AuthOutcome.Failure("请输入密码。")
        return saveAuth(bridge.login(normalizedUsername, password))
    }

    override suspend fun register(
        username: String,
        password: String,
        confirmation: String,
    ): AuthOutcome {
        val normalizedUsername = username.trim()
        if (normalizedUsername.isBlank()) return AuthOutcome.Failure("请输入账号。")
        if (password.length < 6) return AuthOutcome.Failure("密码至少需要 6 位。")
        if (password != confirmation) return AuthOutcome.Failure("两次输入的密码不一致。")
        return saveAuth(bridge.register(normalizedUsername, password, confirmation))
    }

    override suspend fun updateProfile(user: UserProfile): AuthOutcome {
        val token = sessionStore.token()
        if (token.isNullOrBlank()) {
            return AuthOutcome.Failure(
                "登录状态已失效，请重新登录。",
                BridgeError(
                    httpStatus = 401,
                    businessCode = "AUTHENTICATION_REQUIRED",
                    message = "登录状态已失效，请重新登录。",
                    details = null,
                    fieldErrors = emptyMap(),
                    retryable = false,
                    authExpired = true,
                ),
            )
        }
        val update = ProfileUpdate(
            displayName = user.displayName,
            school = user.school,
            major = user.major,
            grade = user.grade,
            education = user.education,
            email = user.email,
            address = user.address,
            profile = user.profile,
        )
        return when (val result = bridge.updateProfile(update)) {
            is BridgeResult.Success -> {
                sessionStore.save(token, result.value)
                mock.resetSessionUser(result.value)
                AuthOutcome.Success(result.value)
            }
            is BridgeResult.Failure -> {
                failure(result.error)
            }
            is BridgeResult.NotConfigured -> AuthOutcome.Failure(result.route)
        }
    }

    override suspend fun logout() {
        val token = sessionStore.token()
        sessionStore.clear()
        mock.clearUserState()
        if (!token.isNullOrBlank()) {
            bridge.logout(token)
        }
    }

    override suspend fun loadResume(): AppResult<ResumeDraft?> =
        mapBridgeResult(bridge.resume())

    override suspend fun saveResumeRemote(draft: ResumeDraft): AppResult<ResumeDraft> {
        val result = mapBridgeResult(bridge.saveResume(draft))
        if (result is AppResult.Success) {
            mock.saveResumeRemote(result.value)
        }
        return result
    }

    override suspend fun loadCatalog(
        query: CatalogQuery,
    ): AppResult<PagedData<CatalogItem>> = bridge.catalog(
        CatalogBridgeQuery(
            kind = query.kind,
            query = query.query,
            city = query.city,
            role = query.role,
            topic = query.topic,
            difficulty = query.difficulty,
            page = query.page,
            pageSize = query.pageSize,
        ),
    ).let(::mapBridgeResult)

    override suspend fun loadJobs(
        search: String,
        page: Int,
        pageSize: Int,
    ): AppResult<PagedData<JobSummary>> = mapBridgeResult(bridge.jobs(search, page, pageSize))

    override suspend fun loadSeniorAdvice(
        search: String,
        ordering: String,
        page: Int,
        pageSize: Int,
    ): AppResult<PagedData<SeniorAdvice>> =
        mapBridgeResult(bridge.seniorAdvice(search, ordering, page, pageSize))

    override suspend fun retrievePracticeQuestions(
        request: PracticeRetrieveRequest,
    ): AppResult<PracticeRetrieval> = mapBridgeResult(bridge.retrieveQuestions(request))

    override suspend fun analyzeResumeRemote(
        jdText: String,
        revision: Int,
        requestId: String,
    ): AppResult<Unit> = bridge.analyzeResume(
        ResumeAnalysisRequest(
            jdText = jdText,
            resumeRevision = revision,
            requestId = requestId,
        ),
    ).let(::mapBridgeResult).map { Unit }

    override suspend fun applyResumeAnalysisRemote(
        analysisId: String,
        baseRevision: Int,
        suggestionIds: List<String>,
        requestId: String,
    ): AppResult<Unit> = bridge.applyResumeAnalysis(
        ResumeApplyRequest(
            analysisId = analysisId,
            baseRevision = baseRevision,
            acceptedSuggestionIds = suggestionIds,
            requestId = requestId,
        ),
    ).let(::mapBridgeResult).map { Unit }

    private fun saveAuth(result: BridgeResult<AuthSession>): AuthOutcome = when (result) {
        is BridgeResult.Success -> {
            sessionStore.save(result.value.token, result.value.user)
            mock.resetSessionUser(result.value.user)
            AuthOutcome.Success(result.value.user)
        }
        is BridgeResult.Failure -> failure(result.error)
        is BridgeResult.NotConfigured -> AuthOutcome.Failure(result.route)
    }

    private fun failure(error: BridgeError): AuthOutcome.Failure {
        if (error.authExpired) {
            sessionStore.clear()
            mock.clearUserState()
        }
        return AuthOutcome.Failure(error.message, error)
    }

    private fun <T> mapBridgeResult(result: BridgeResult<T>): AppResult<T> = when (result) {
        is BridgeResult.Success -> AppResult.Success(result.value)
        is BridgeResult.Failure -> {
            if (result.error.authExpired) {
                sessionStore.clear()
                mock.clearUserState()
                invalidations.tryEmit(Unit)
            }
            AppResult.Failure(result.error)
        }
        is BridgeResult.NotConfigured -> AppResult.Failure(
            BridgeError(
                httpStatus = null,
                businessCode = "CONFIGURATION_ERROR",
                message = result.route,
                details = null,
                fieldErrors = emptyMap(),
                retryable = false,
                authExpired = false,
            ),
        )
    }
}

private inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}
