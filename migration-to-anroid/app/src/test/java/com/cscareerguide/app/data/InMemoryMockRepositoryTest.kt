package com.cscareerguide.app.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryMockRepositoryTest {
    @Test
    fun initialDataIsStableAndOffline() {
        val repository = InMemoryMockRepository()

        assertNull(repository.currentUser())
        assertEquals(4, repository.careerPaths().size)
        assertEquals(
            listOf("employment", "postgraduate", "civil_service", "entrepreneurship"),
            repository.careerPaths().map { it.id },
        )
        assertEquals(4, repository.homeMetrics().careerPathCount)
        assertEquals(5, repository.jobs().size)
        assertEquals(3, repository.companyRankings(true).size)
        assertEquals(3, repository.companyRankings(false).size)
        assertEquals(2, repository.seniorAdvice().size)
        assertEquals("示例同学", repository.resumeDraft().name)
        assertEquals(4, repository.interviewConfig().styles.size)
        assertEquals(7, repository.catalogItems().size)
        assertEquals(3, repository.resumeAnalysis(3).suggestions.size)
        assertEquals(3, repository.practiceQuestions().size)
        assertEquals(3, repository.practiceRecords().size)
        assertEquals(2, repository.evaluationRuns().size)
    }

    @Test
    fun loginCreatesOnlyAnInMemorySession() = runTest {
        val repository = InMemoryMockRepository()

        val failure = repository.login("student", "123")
        assertTrue(failure is AuthOutcome.Failure)
        assertNull(repository.currentUser())

        val success = repository.login("student", "123456")
        assertTrue(success is AuthOutcome.Success)
        assertEquals("student", repository.currentUser()?.username)

        repository.logout()
        assertNull(repository.currentUser())
    }

    @Test
    fun registrationValidatesPasswordConfirmation() = runTest {
        val repository = InMemoryMockRepository()

        val mismatch = repository.register("student", "123456", "654321")
        assertTrue(mismatch is AuthOutcome.Failure)
        assertEquals("两次输入的密码不一致。", (mismatch as AuthOutcome.Failure).message)

        val success = repository.register("student", "123456", "123456")
        assertTrue(success is AuthOutcome.Success)
        assertEquals("student", repository.currentUser()?.username)
    }
}
