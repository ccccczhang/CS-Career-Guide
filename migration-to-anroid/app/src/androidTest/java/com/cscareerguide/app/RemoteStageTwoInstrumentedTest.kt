package com.cscareerguide.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cscareerguide.app.data.AppResult
import com.cscareerguide.app.data.AuthOutcome
import com.cscareerguide.app.data.CatalogQuery
import com.cscareerguide.app.data.RemoteCareerRepository
import com.cscareerguide.app.data.bridge.HttpCareerBridge
import com.cscareerguide.app.data.model.CatalogKind
import com.cscareerguide.app.data.model.ResumeDraft
import com.cscareerguide.app.data.session.EncryptedAuthSessionStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoteStageTwoInstrumentedTest {
    @Test
    fun resumeDirectoryJobsAndAdviceUseServerContracts() = runBlocking {
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
        assertTrue(
            repository.login(ServerTestAccount.username, ServerTestAccount.password)
                is AuthOutcome.Success,
        )

        val saved = repository.saveResumeRemote(
            ResumeDraft(name = "阶段二测试", targetPosition = "Java后端", skills = "Java,SQL"),
        )
        assertTrue(saved is AppResult.Success)
        val savedDraft = (saved as AppResult.Success).value
        assertTrue(savedDraft.id.isNotBlank())

        val loaded = repository.loadResume()
        assertTrue(loaded is AppResult.Success)
        assertEquals("阶段二测试", (loaded as AppResult.Success).value?.name)

        for (kind in CatalogKind.entries) {
            val page = repository.loadCatalog(
                CatalogQuery(kind = kind, pageSize = 5),
            )
            assertTrue("catalog $kind failed: $page", page is AppResult.Success)
        }
        assertTrue(repository.loadJobs("", 1, 5) is AppResult.Success)
        assertTrue(repository.loadSeniorAdvice("", "recent", 1, 5) is AppResult.Success)

        repository.logout()
        assertNull(store.token())
    }
}
