package com.cscareerguide.app.ui

import com.cscareerguide.app.data.InMemoryMockRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loginExposesLoadingThenAuthenticatedState() = runTest {
        val viewModel = AppViewModel(
            repository = InMemoryMockRepository(),
            authDelayMillis = 100,
        )

        viewModel.login("student", "123456")
        runCurrent()
        assertTrue(viewModel.uiState.value.authLoading)
        assertNull(viewModel.uiState.value.user)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.authLoading)
        assertEquals("student", viewModel.uiState.value.user?.username)
        assertEquals(4, viewModel.uiState.value.careerPaths.size)
    }

    @Test
    fun invalidLoginShowsAResultInsteadOfFakeSuccess() = runTest {
        val viewModel = AppViewModel(
            repository = InMemoryMockRepository(),
            authDelayMillis = 0,
        )

        viewModel.login("student", "123")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.user)
        assertEquals("密码至少需要 6 位。", viewModel.uiState.value.authError)

        viewModel.clearAuthError()
        assertNull(viewModel.uiState.value.authError)
    }

    @Test
    fun registrationRequiresMatchingPasswords() = runTest {
        val viewModel = AppViewModel(
            repository = InMemoryMockRepository(),
            authDelayMillis = 0,
        )

        viewModel.register("student", "123456", "654321")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.user)
        assertEquals("两次输入的密码不一致。", viewModel.uiState.value.authError)
    }

    @Test
    fun logoutClearsUserButKeepsMockContent() = runTest {
        val viewModel = AppViewModel(
            repository = InMemoryMockRepository(),
            authDelayMillis = 0,
        )

        viewModel.login("student", "123456")
        advanceUntilIdle()
        viewModel.logout()

        assertNull(viewModel.uiState.value.user)
        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertEquals(4, viewModel.uiState.value.careerPaths.size)
    }

    @Test
    fun profileEditUpdatesTheActiveMockSession() = runTest {
        val viewModel = AppViewModel(
            repository = InMemoryMockRepository(),
            authDelayMillis = 0,
        )
        viewModel.login("student", "123456")
        advanceUntilIdle()

        viewModel.updateProfile(
            displayName = "新同学",
            school = "新示例大学",
            major = "软件工程",
            grade = "大四",
            education = "本科",
            email = "new@example.com",
            address = "杭州",
            profile = "正在准备后端开发校招。",
        )

        assertEquals("新同学", viewModel.uiState.value.user?.displayName)
        assertEquals("软件工程", viewModel.uiState.value.user?.major)
        assertEquals("杭州", viewModel.uiState.value.user?.address)
    }
}
