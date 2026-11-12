package com.cscareerguide.app.ui.careerprep

import com.cscareerguide.app.data.InMemoryMockRepository
import com.cscareerguide.app.ui.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CareerPrepViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun jobSearchFiltersWithoutChangingTheSourceData() {
        val viewModel = CareerPrepViewModel(
            repository = InMemoryMockRepository(),
            mockDelayMillis = 0,
        )

        viewModel.updateSearch("杭州")

        assertEquals(1, viewModel.huntingState.value.visibleJobs.size)
        assertEquals("前端开发工程师", viewModel.huntingState.value.visibleJobs.first().title)
        assertEquals(5, viewModel.huntingState.value.jobs.size)
    }

    @Test
    fun adviceFormValidatesAndAddsAnOfflineRecord() = runTest {
        val viewModel = CareerPrepViewModel(
            repository = InMemoryMockRepository(),
            mockDelayMillis = 0,
        )

        viewModel.submitAdvice()
        assertNotNull(viewModel.huntingState.value.form.error)

        viewModel.updateAdviceForm {
            it.copy(
                company = "测试公司",
                position = "后端开发",
                author = "测试同学",
                graduationYear = "2026",
                advice = "这是一次完整的本地 Mock 经验分享。",
                rating = "4.5",
            )
        }
        viewModel.submitAdvice()
        advanceUntilIdle()

        assertNull(viewModel.huntingState.value.form.error)
        assertNotNull(viewModel.huntingState.value.form.successMessage)
        assertEquals("测试公司", viewModel.huntingState.value.advice.first().company)
        assertEquals(3, viewModel.huntingState.value.advice.size)
    }

    @Test
    fun resumeEditsStayInStateAndCanBeSaved() = runTest {
        val viewModel = CareerPrepViewModel(
            repository = InMemoryMockRepository(),
            mockDelayMillis = 0,
        )

        viewModel.updateResumeBasic(name = "新名字")
        viewModel.addProject()
        assertEquals("新名字", viewModel.resumeState.value.draft.name)
        assertEquals(2, viewModel.resumeState.value.draft.projects.size)

        viewModel.saveResume()
        advanceUntilIdle()

        assertFalse(viewModel.resumeState.value.saving)
        assertEquals("简历已保存到本地 Mock 会话。", viewModel.resumeState.value.notice)
    }

    @Test
    fun interviewStartsWithTechnicalQuestionAndEndsWithReview() = runTest {
        val viewModel = CareerPrepViewModel(
            repository = InMemoryMockRepository(),
            mockDelayMillis = 0,
        )

        assertEquals("technical", viewModel.interviewState.value.styleId)
        viewModel.startInterview()
        advanceUntilIdle()

        assertTrue(viewModel.interviewState.value.isRunning)
        assertTrue(viewModel.interviewState.value.messages.first().content.contains("技术栈"))

        viewModel.sendInterviewAnswer("我负责接口设计和测试。")
        advanceUntilIdle()
        assertEquals(3, viewModel.interviewState.value.messages.size)

        viewModel.finishInterview()
        advanceUntilIdle()

        assertFalse(viewModel.interviewState.value.isRunning)
        assertNotNull(viewModel.interviewState.value.review)
        assertEquals(78, viewModel.interviewState.value.review?.score)
    }
}
