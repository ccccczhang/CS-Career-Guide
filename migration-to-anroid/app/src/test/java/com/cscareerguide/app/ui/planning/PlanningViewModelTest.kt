package com.cscareerguide.app.ui.planning

import com.cscareerguide.app.data.InMemoryMockRepository
import com.cscareerguide.app.ui.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlanningViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun mockPlanningDataPreservesTheWebBaseline() {
        val repository = InMemoryMockRepository()

        assertEquals(listOf(6, 6, 6, 6), repository.skillGroups().map { it.skills.size })
        assertEquals(9, repository.careerCategories().size)
        assertEquals(
            62,
            repository.careerCategories()
                .flatMap { it.roles }
                .toSet()
                .size,
        )
    }

    @Test
    fun draftIsAutomaticallySavedAfterTheDelay() = runTest {
        val repository = InMemoryMockRepository()
        val viewModel = PlanningViewModel(repository, draftSaveDelayMillis = 600)

        viewModel.updateIntroduction { it.copy(name = "示例同学") }
        assertEquals(DraftSaveStatus.PENDING, viewModel.uiState.value.draftStatus)

        advanceTimeBy(600)
        runCurrent()

        assertEquals(DraftSaveStatus.SAVED, viewModel.uiState.value.draftStatus)
        assertEquals("示例同学", repository.introductionDraft().name)
    }

    @Test
    fun skillSelectionIsDeduplicated() {
        val viewModel = PlanningViewModel(InMemoryMockRepository(), draftSaveDelayMillis = 0)

        viewModel.toggleSkill("Python 开发")
        viewModel.toggleSkill("Python 开发")
        assertTrue(viewModel.uiState.value.introduction.skills.isEmpty())

        viewModel.toggleSkill("Python 开发")
        assertEquals(setOf("Python 开发"), viewModel.uiState.value.introduction.skills)
    }

    @Test
    fun incompleteIntroductionCannotAdvance() {
        val viewModel = PlanningViewModel(InMemoryMockRepository(), draftSaveDelayMillis = 0)

        assertFalse(viewModel.finishIntroduction())
        assertEquals(
            "请先填写姓名、学校、专业、年级、目标职业，并至少选择一项技能。",
            viewModel.uiState.value.introductionError,
        )
    }

    @Test
    fun completeIntroductionProducesRecommendationsAndReport() {
        val viewModel = PlanningViewModel(InMemoryMockRepository(), draftSaveDelayMillis = 0)
        viewModel.updateIntroduction {
            it.copy(
                name = "示例同学",
                school = "示例大学",
                major = "计算机科学与技术",
                grade = "大三",
                goal = "后端工程师",
                otherSkills = "完成过课程项目",
                selfDescription = "喜欢结构化解决问题",
            )
        }
        viewModel.toggleSkill("Python 开发")
        viewModel.toggleSkill("PostgreSQL 数据库")

        assertTrue(viewModel.finishIntroduction())
        assertTrue(viewModel.uiState.value.recommendations.isNotEmpty())

        viewModel.selectCareer("Java")
        assertTrue(viewModel.createReport())
        assertEquals("Java", viewModel.uiState.value.report?.targetCareer)
        assertNull(viewModel.uiState.value.introductionError)
    }
}
