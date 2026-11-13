package com.cscareerguide.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cscareerguide.app.data.AuthOutcome
import com.cscareerguide.app.data.RemoteCareerRepository
import com.cscareerguide.app.data.bridge.HttpCareerBridge
import com.cscareerguide.app.data.session.EncryptedAuthSessionStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoteAuthInstrumentedTest {
    @Test
    fun serverLoginRestoresProfileAndClearsEncryptedSessionOnLogout() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = EncryptedAuthSessionStore(context)
        store.clear()
        val repository = repository(store)

        val login = repository.login(ServerTestAccount.username, ServerTestAccount.password)
        assertTrue(login is AuthOutcome.Success)
        assertEquals(
            ServerTestAccount.username,
            (login as AuthOutcome.Success).user.username,
        )
        assertTrue(store.token().orEmpty().isNotBlank())

        val reopenedStore = EncryptedAuthSessionStore(context)
        assertTrue(reopenedStore.token().orEmpty().isNotBlank())
        val restored = repository(reopenedStore).restoreSession()
        assertTrue(restored is AuthOutcome.Success)
        assertEquals(
            ServerTestAccount.username,
            (restored as AuthOutcome.Success).user.username,
        )

        repository(reopenedStore).logout()
        assertNull(EncryptedAuthSessionStore(context).token())
        assertNull(repository(reopenedStore).currentUser())
    }

    private fun repository(store: EncryptedAuthSessionStore) = RemoteCareerRepository(
        bridge = HttpCareerBridge(
            apiBaseUrl = BuildConfig.API_BASE_URL,
            sessionStore = store,
            allowCleartext = BuildConfig.ALLOW_CLEARTEXT,
        ),
        sessionStore = store,
    )
}
