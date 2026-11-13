package com.cscareerguide.app.data

import com.cscareerguide.app.data.bridge.AuthSession
import com.cscareerguide.app.data.bridge.BridgeError
import com.cscareerguide.app.data.bridge.BridgeResult
import com.cscareerguide.app.data.bridge.CatalogBridgeQuery
import com.cscareerguide.app.data.bridge.CareerBridge
import com.cscareerguide.app.data.bridge.InterviewChatRequest
import com.cscareerguide.app.data.bridge.InterviewStreamEvent
import com.cscareerguide.app.data.bridge.InterviewStreamResult
import com.cscareerguide.app.data.bridge.PracticeRetrieveRequest
import com.cscareerguide.app.data.bridge.ProfileUpdate
import com.cscareerguide.app.data.bridge.ResumeAnalysisRequest
import com.cscareerguide.app.data.bridge.ResumeApplyRequest
import com.cscareerguide.app.data.model.CatalogItem
import com.cscareerguide.app.data.model.JobSummary
import com.cscareerguide.app.data.model.PracticeQuestion
import com.cscareerguide.app.data.model.PracticeRetrieval
import com.cscareerguide.app.data.model.ResumeDraft
import com.cscareerguide.app.data.model.SeniorAdvice
import com.cscareerguide.app.data.model.UserProfile
import com.cscareerguide.app.data.session.AuthSessionStore
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteCareerRepositoryTest {
    @Test
    fun loginPersistsTokenAndUser() = runTest {
        val store = MemorySessionStore()
        val repository = RemoteCareerRepository(FakeBridge(), store)

        val result = repository.login("test_a", "secret")

        assertTrue(result is AuthOutcome.Success)
        assertEquals("token-a", store.token())
        assertEquals("test_a", store.user()?.username)
    }

    @Test
    fun logoutClearsLocalSessionWhenRemoteLogoutFails() = runTest {
        val store = MemorySessionStore()
        val bridge = FakeBridge(
            logoutResult = BridgeResult.Failure(
                "/api/users/auth/logout/",
                BridgeError(
                    httpStatus = 503,
                    businessCode = "UNAVAILABLE",
                    message = "unavailable",
                    details = null,
                    fieldErrors = emptyMap(),
                    retryable = true,
                    authExpired = false,
                ),
            ),
        )
        val repository = RemoteCareerRepository(bridge, store)
        repository.login("test_a", "secret")

        repository.logout()

        assertNull(store.token())
        assertNull(repository.currentUser())
    }

    @Test
    fun unauthorizedSessionRestoreClearsUserScopedData() = runTest {
        val store = MemorySessionStore()
        val bridge = FakeBridge(
            profileResult = BridgeResult.Failure(
                "/api/users/profile/",
                BridgeError(
                    httpStatus = 401,
                    businessCode = null,
                    message = "expired",
                    details = null,
                    fieldErrors = emptyMap(),
                    retryable = false,
                    authExpired = true,
                ),
            ),
        )
        val repository = RemoteCareerRepository(bridge, store)
        repository.login("test_a", "secret")
        repository.saveResume(ResumeDraft(name = "sensitive"))

        val restored = repository.restoreSession()

        assertTrue(restored is AuthOutcome.Failure)
        assertNull(store.token())
        assertTrue(repository.resumeDraft().name.isBlank())
    }

    @Test
    fun retrievalMapsBridgeReceiptAndDoesNotFallBackToMock() = runTest {
        val store = MemorySessionStore()
        val question = PracticeQuestion(
            id = "30000000-0000-4000-8000-000000000001",
            question = "List 与 Set 的区别是什么？",
            role = "Java后端",
            topic = "集合",
            difficulty = "easy",
            keyPoints = listOf("List 允许重复元素"),
            sourceEvidence = listOf("source: 团队自编示例"),
            matchScore = 0.6,
        )
        val expected = PracticeRetrieval(
            count = 1,
            retrievalId = "20000000-0000-4000-8000-000000000001",
            message = "",
            results = listOf(question),
        )
        val repository = RemoteCareerRepository(
            FakeBridge(retrieveResult = BridgeResult.Success(expected)),
            store,
        )

        val result = repository.retrievePracticeQuestions(
            PracticeRetrieveRequest(
                role = "Java后端",
                topic = "集合",
                requestId = "10000000-0000-4000-8000-000000000001",
            ),
        )

        assertTrue(result is AppResult.Success)
        assertEquals(expected, (result as AppResult.Success).value)
    }

    private class MemorySessionStore : AuthSessionStore {
        private var savedToken: String? = null
        private var savedUser: UserProfile? = null
        override fun token(): String? = savedToken
        override fun user(): UserProfile? = savedUser
        override fun save(token: String, user: UserProfile) {
            savedToken = token
            savedUser = user
        }
        override fun clear() {
            savedToken = null
            savedUser = null
        }
    }

    private class FakeBridge(
        private val logoutResult: BridgeResult<Unit> = BridgeResult.Success(Unit),
        private val profileResult: BridgeResult<UserProfile>? = null,
        private val retrieveResult: BridgeResult<PracticeRetrieval> =
            BridgeResult.NotConfigured("pending"),
    ) : CareerBridge {
        override suspend fun login(username: String, password: String): BridgeResult<AuthSession> =
            BridgeResult.Success(
                AuthSession(
                    user = user(username),
                    token = "token-a",
                ),
            )

        override suspend fun register(
            username: String,
            password: String,
            confirmation: String,
        ): BridgeResult<AuthSession> = login(username, password)

        override suspend fun logout(token: String): BridgeResult<Unit> = logoutResult
        override suspend fun profile(): BridgeResult<UserProfile> =
            profileResult ?: BridgeResult.Success(user("test_a"))

        override suspend fun updateProfile(update: ProfileUpdate): BridgeResult<UserProfile> =
            BridgeResult.Success(user("test_a").copy(displayName = update.displayName))

        override suspend fun resume(): BridgeResult<ResumeDraft?> = BridgeResult.Success(null)
        override suspend fun saveResume(draft: ResumeDraft): BridgeResult<ResumeDraft> =
            BridgeResult.Success(draft)
        override suspend fun uploadResumeAvatar(
            bytes: ByteArray,
            fileName: String,
            mediaType: String,
        ): BridgeResult<String> = pending()
        override suspend fun deleteResumeAvatar(): BridgeResult<Unit> = pending()
        override suspend fun catalog(
            query: CatalogBridgeQuery,
        ): BridgeResult<PagedData<CatalogItem>> = pending()
        override suspend fun analyzeResume(
            request: ResumeAnalysisRequest,
        ): BridgeResult<JsonElement> = pending()
        override suspend fun applyResumeAnalysis(
            request: ResumeApplyRequest,
        ): BridgeResult<JsonElement> = pending()
        override suspend fun jobs(
            search: String,
            page: Int,
            pageSize: Int,
        ): BridgeResult<PagedData<JobSummary>> = pending()
        override suspend fun seniorAdvice(
            search: String,
            ordering: String,
            page: Int,
            pageSize: Int,
        ): BridgeResult<PagedData<SeniorAdvice>> = pending()
        override suspend fun retrieveQuestions(
            request: PracticeRetrieveRequest,
        ): BridgeResult<PracticeRetrieval> = retrieveResult
        override suspend fun listPracticeSessions(): BridgeResult<Unit> = pending()
        override suspend fun listEvaluationRuns(): BridgeResult<Unit> = pending()
        override suspend fun streamInterviewChat(
            request: InterviewChatRequest,
            onEvent: (InterviewStreamEvent) -> Unit,
        ): BridgeResult<InterviewStreamResult> = pending()

        private fun user(username: String) = UserProfile(
            id = "1",
            username = username,
            displayName = username,
            school = "",
            major = "",
            grade = "",
            profile = "",
        )

        private fun <T> pending(): BridgeResult<T> = BridgeResult.NotConfigured("pending")
    }
}
