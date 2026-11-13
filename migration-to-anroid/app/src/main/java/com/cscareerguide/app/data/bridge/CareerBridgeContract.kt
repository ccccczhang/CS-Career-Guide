package com.cscareerguide.app.data.bridge

import com.cscareerguide.app.data.PagedData
import com.cscareerguide.app.data.model.CatalogKind
import com.cscareerguide.app.data.model.CatalogItem
import com.cscareerguide.app.data.model.JobSummary
import com.cscareerguide.app.data.model.PracticeRetrieval
import com.cscareerguide.app.data.model.ResumeDraft
import com.cscareerguide.app.data.model.SeniorAdvice
import com.cscareerguide.app.data.model.UserProfile
import kotlinx.serialization.json.JsonElement

/**
 * Backend route registry for the Android bridge.
 *
 * BuildConfig.API_BASE_URL is the origin without [API_PREFIX].
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
        java.net.URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")
}

sealed interface BridgeResult<out T> {
    data class Success<T>(val value: T) : BridgeResult<T>
    data class NotConfigured(val route: String) : BridgeResult<Nothing>
    data class Failure(
        val route: String,
        val error: BridgeError,
    ) : BridgeResult<Nothing>
}

data class BridgeError(
    val httpStatus: Int?,
    val businessCode: String?,
    val message: String,
    val details: JsonElement?,
    val fieldErrors: Map<String, List<String>>,
    val retryable: Boolean,
    val authExpired: Boolean,
)

data class AuthSession(
    val user: UserProfile,
    val token: String,
)

data class ProfileUpdate(
    val displayName: String,
    val school: String,
    val major: String,
    val grade: String,
    val education: String,
    val email: String,
    val address: String,
    val profile: String,
)

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
    val topic: String = "",
    val difficulty: String = "easy",
    val jobId: Long? = null,
    val jdText: String = "",
    val topK: Int = 5,
    val requestId: String,
)

data class InterviewChatRequest(
    val message: String,
    val mode: String = "interview",
    val style: String = "technical",
    val sessionId: String = "",
    val enableTts: Boolean = false,
)

sealed interface InterviewStreamEvent {
    data class Chunk(val content: String) : InterviewStreamEvent
    data class Audio(val base64: String) : InterviewStreamEvent
    data class Session(val sessionId: String) : InterviewStreamEvent
    data class ServerError(val message: String) : InterviewStreamEvent
    data object Done : InterviewStreamEvent
}

data class InterviewStreamResult(
    val content: String,
    val sessionId: String?,
    val done: Boolean,
    val ended: Boolean,
)

/**
 * Adapter boundary for the real backend.
 *
 * Screens and ViewModels must keep depending on repository/UI state instead of calling
 * this interface directly. The TODO markers are deliberate hand-off points.
 */
interface CareerBridge {
    suspend fun login(username: String, password: String): BridgeResult<AuthSession>

    suspend fun register(
        username: String,
        password: String,
        confirmation: String,
    ): BridgeResult<AuthSession>

    suspend fun logout(token: String): BridgeResult<Unit>

    suspend fun profile(): BridgeResult<UserProfile>

    suspend fun updateProfile(update: ProfileUpdate): BridgeResult<UserProfile>

    suspend fun catalog(query: CatalogBridgeQuery): BridgeResult<PagedData<CatalogItem>>

    suspend fun resume(): BridgeResult<ResumeDraft?>

    suspend fun saveResume(draft: ResumeDraft): BridgeResult<ResumeDraft>

    suspend fun uploadResumeAvatar(
        bytes: ByteArray,
        fileName: String,
        mediaType: String,
    ): BridgeResult<String>

    suspend fun deleteResumeAvatar(): BridgeResult<Unit>

    suspend fun analyzeResume(request: ResumeAnalysisRequest): BridgeResult<JsonElement>

    suspend fun applyResumeAnalysis(request: ResumeApplyRequest): BridgeResult<JsonElement>

    suspend fun jobs(
        search: String,
        page: Int,
        pageSize: Int,
    ): BridgeResult<PagedData<JobSummary>>

    suspend fun seniorAdvice(
        search: String,
        ordering: String,
        page: Int,
        pageSize: Int,
    ): BridgeResult<PagedData<SeniorAdvice>>

    suspend fun retrieveQuestions(request: PracticeRetrieveRequest): BridgeResult<PracticeRetrieval>

    // TODO(bridge): implement practice session create/answer/retry/finish and record mapping.
    suspend fun listPracticeSessions(): BridgeResult<Unit>

    // TODO(bridge): enforce admin capability from server response; never infer is_staff from profile.
    suspend fun listEvaluationRuns(): BridgeResult<Unit>

    suspend fun streamInterviewChat(
        request: InterviewChatRequest,
        onEvent: (InterviewStreamEvent) -> Unit,
    ): BridgeResult<InterviewStreamResult>

    // TODO(bridge): transport microphone PCM uploads separately.
}
