package com.cscareerguide.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cscareerguide.app.data.AppResult
import com.cscareerguide.app.data.AuthOutcome
import com.cscareerguide.app.data.RemoteCareerRepository
import com.cscareerguide.app.data.bridge.HttpCareerBridge
import com.cscareerguide.app.data.bridge.PracticeRetrieveRequest
import com.cscareerguide.app.data.session.EncryptedAuthSessionStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoteStageThreeInstrumentedTest {
    @Test
    fun formalQuestionRetrievalUsesConfiguredServer() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = EncryptedAuthSessionStore(context)
        store.clear()
        val repository = RemoteCareerRepository(
            bridge = HttpCareerBridge(
                apiBaseUrl = BuildConfig.API_BASE_URL,
                sessionStore = store,
                allowCleartext = BuildConfig.ALLOW_CLEARTEXT,
            ),
            sessionStore = store,
        )

        try {
            assertTrue(
                repository.login(ServerTestAccount.username, ServerTestAccount.password)
                    is AuthOutcome.Success,
            )

            val result = repository.retrievePracticeQuestions(
                PracticeRetrieveRequest(
                    role = "Java后端",
                    topic = "集合",
                    difficulty = "easy",
                    jdText = "Java 集合",
                    topK = 5,
                    requestId = "10000000-0000-4000-8000-000000000011",
                ),
            )

            assertTrue("retrieve failed: $result", result is AppResult.Success)
            val retrieval = (result as AppResult.Success).value
            assertTrue(retrieval.retrievalId.isNotBlank())
            assertEquals(retrieval.results.size, retrieval.count)
            retrieval.results.forEach { question ->
                assertTrue(question.id.isNotBlank())
                assertTrue(question.question.isNotBlank())
            }
        } finally {
            repository.logout()
            assertNull(store.token())
        }
    }
}
