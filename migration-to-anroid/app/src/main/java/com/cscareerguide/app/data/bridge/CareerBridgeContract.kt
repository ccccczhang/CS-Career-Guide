package com.cscareerguide.app.data.bridge

import com.cscareerguide.app.data.model.CatalogKind

/**
 * Backend route registry for the future Android bridge.
 *
 * TODO(bridge): keep these paths aligned with CS-Career-Guide/docs/app-api.md.
 * TODO(bridge): resolve [API_PREFIX] through a non-secret build configuration.
 */
object BridgeRoutes {
    const val API_PREFIX = "/api"

    const val LOGIN = "$API_PREFIX/users/auth/login/"
    const val REGISTER = "$API_PREFIX/users/auth/register/"
    const val LOGOUT = "$API_PREFIX/users/auth/logout/"
    const val PROFILE = "$API_PREFIX/users/profile/"
    const val SET_PASSWORD = "$API_PREFIX/users/profile/set-password/"

    const val RESUME = "$API_PREFIX/resumes/my/"
    const val RESUME_AVATAR_UPLOAD = "$API_PREFIX/resumes/upload_avatar/"
    const val RESUME_AVATAR_DELETE = "$API_PREFIX/resumes/delete_avatar/"

    const val COMPANY_JOBS = "$API_PREFIX/career-evaluation/computer-careers/"
    const val SENIOR_ADVICE = "$API_PREFIX/ai/senior-advice/"
    const val INTERVIEW_CHAT = "$API_PREFIX/ai/llm/chat/"
    const val INTERVIEW_HISTORY = "$API_PREFIX/ai/llm/chat/history/"
    const val INTERVIEW_REVIEWS = "$API_PREFIX/ai/records/interview-reviews/"
    const val INTERVIEW_SAVE_REVIEW = "$API_PREFIX/ai/interview/save_review/"
    const val ASR = "$API_PREFIX/asr/speech-to-text/"

    const val CATALOG_IMPORTS = "$API_PREFIX/career-platform/catalog/imports/"
    const val RESUME_ANALYSES = "$API_PREFIX/career-platform/resume-analyses/"
    const val QUESTION_RETRIEVE = "$API_PREFIX/career-platform/questions/retrieve/"
    const val QUESTION_RETRIEVE_DEMO = "$API_PREFIX/career-platform/questions/demo-retrieve/"
    const val PRACTICE_SESSIONS = "$API_PREFIX/career-platform/practice-sessions/"
    const val EVALUATION_RUNS = "$API_PREFIX/career-platform/evaluation-runs/"

    fun catalog(kind: CatalogKind): String =
        "$API_PREFIX/career-platform/catalog/${kind.name.lowercase()}/"

    fun resumeAnalysis(id: String): String =
        "$RESUME_ANALYSES${encodePath(id)}/"

    fun applyResumeAnalysis(id: String): String =
        "${resumeAnalysis(id)}apply/"

    fun practiceSession(id: String): String =
        "$PRACTICE_SESSIONS${encodePath(id)}/"

    fun practiceAnswers(id: String): String =
        "${practiceSession(id)}answers/"

    fun retryPracticeFeedback(sessionId: String, answerId: String): String =
        "${practiceAnswers(sessionId)}${encodePath(answerId)}/retry-feedback/"

    fun finishPracticeSession(id: String): String =
        "${practiceSession(id)}finish/"

    fun evaluationRun(id: String): String =
        "$EVALUATION_RUNS${encodePath(id)}/"

    fun exportEvaluation(id: String): String =
        "${evaluationRun(id)}export/"

    private fun encodePath(value: String): String =
        java.net.URLEncoder.encode(value, Charsets.UTF_8.name())
}

sealed interface BridgeResult<out T> {
    data class Success<T>(val value: T) : BridgeResult<T>
    data class NotConfigured(val route: String) : BridgeResult<Nothing>
    data class Failure(
        val route: String,
        val code: Int?,
        val message: String,
    ) : BridgeResult<Nothing>
}

data class BridgeAuthorization(
    val token: String,
)

data class CatalogBridgeQuery(
    val kind: CatalogKind,
    val query: String = "",
    val city: String = "",
    val role: String = "",
    val topic: String = "",
    val difficulty: String = "",
    val page: Int = 1,
    val pageSize: Int = 20,
)

data class ResumeAnalysisRequest(
    val jdText: String,
    val resumeRevision: Int,
    val requestId: String,
)

data class ResumeApplyRequest(
    val analysisId: String,
    val baseRevision: Int,
    val acceptedSuggestionIds: List<String>,
    val requestId: String,
)

data class PracticeRetrieveRequest(
    val role: String,
    val topic: String,
    val difficulty: String,
    val query: String,
    val topK: Int,
)

/**
 * Adapter boundary for the real backend.
 *
 * Screens and ViewModels must keep depending on repository/UI state instead of calling
 * this interface directly. The TODO markers are deliberate hand-off points.
 */
interface CareerBridge {
    // TODO(bridge): implement auth/profile calls, including Token header and multipart avatar upload.
    suspend fun login(username: String, password: String): BridgeResult<Unit>

    // TODO(bridge): map paginated catalog responses, permission_status and source metadata.
    suspend fun catalog(query: CatalogBridgeQuery): BridgeResult<Unit>

    // TODO(bridge): send UUID request_id once and reuse it when retrying the same operation.
    suspend fun analyzeResume(request: ResumeAnalysisRequest): BridgeResult<Unit>

    suspend fun applyResumeAnalysis(request: ResumeApplyRequest): BridgeResult<Unit>

    suspend fun retrieveQuestions(request: PracticeRetrieveRequest): BridgeResult<Unit>

    // TODO(bridge): implement practice session create/answer/retry/finish and record mapping.
    suspend fun listPracticeSessions(): BridgeResult<Unit>

    // TODO(bridge): enforce admin capability from server response; never infer is_staff from profile.
    suspend fun listEvaluationRuns(): BridgeResult<Unit>

    // TODO(bridge): parse SSE frames and transport microphone PCM uploads separately.
    suspend fun streamInterviewChat(
        sessionId: String,
        message: String,
        onEvent: (String) -> Unit,
    ): BridgeResult<Unit>
}

class UnconfiguredCareerBridge : CareerBridge {
    override suspend fun login(username: String, password: String): BridgeResult<Unit> =
        BridgeResult.NotConfigured(BridgeRoutes.LOGIN)

    override suspend fun catalog(query: CatalogBridgeQuery): BridgeResult<Unit> =
        BridgeResult.NotConfigured(BridgeRoutes.catalog(query.kind))

    override suspend fun analyzeResume(request: ResumeAnalysisRequest): BridgeResult<Unit> =
        BridgeResult.NotConfigured(BridgeRoutes.RESUME_ANALYSES)

    override suspend fun applyResumeAnalysis(request: ResumeApplyRequest): BridgeResult<Unit> =
        BridgeResult.NotConfigured(BridgeRoutes.applyResumeAnalysis(request.analysisId))

    override suspend fun retrieveQuestions(request: PracticeRetrieveRequest): BridgeResult<Unit> =
        BridgeResult.NotConfigured(BridgeRoutes.QUESTION_RETRIEVE_DEMO)

    override suspend fun listPracticeSessions(): BridgeResult<Unit> =
        BridgeResult.NotConfigured(BridgeRoutes.PRACTICE_SESSIONS)

    override suspend fun listEvaluationRuns(): BridgeResult<Unit> =
        BridgeResult.NotConfigured(BridgeRoutes.EVALUATION_RUNS)

    override suspend fun streamInterviewChat(
        sessionId: String,
        message: String,
        onEvent: (String) -> Unit,
    ): BridgeResult<Unit> = BridgeResult.NotConfigured(BridgeRoutes.INTERVIEW_CHAT)
}
