package com.cscareerguide.app.ui.workspace

import com.cscareerguide.app.data.InMemoryMockRepository
import com.cscareerguide.app.data.model.CatalogKind
import com.cscareerguide.app.ui.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkspaceViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun catalogKindAndQueryFilterLocalRecords() = runTest {
        val viewModel = WorkspaceViewModel(InMemoryMockRepository(), mockDelayMillis = 0)

        viewModel.selectCatalogKind(CatalogKind.QUESTIONS)
        viewModel.updateCatalogFilters(query = "TCP")
        viewModel.searchCatalog()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.catalog.items.size)
        assertEquals("TCP 为什么需要三次握手？", viewModel.uiState.value.catalog.items.first().title)
        assertFalse(viewModel.uiState.value.catalog.loading)
    }

    @Test
    fun resumeAnalysisRequiresJdAndSelectedSuggestion() = runTest {
        val viewModel = WorkspaceViewModel(InMemoryMockRepository(), mockDelayMillis = 0)

        viewModel.analyzeResume()
        assertTrue(viewModel.uiState.value.resumeOptimization.isError)

        viewModel.updateJdText("要求掌握 Java、Spring Boot 和 SQL，能够独立交付接口。")
        viewModel.analyzeResume()
        advanceUntilIdle()

        val analysis = viewModel.uiState.value.resumeOptimization.analysis
        assertNotNull(analysis)
        assertEquals(3, analysis?.suggestions?.size)

        viewModel.applyResumeSuggestions()
        assertTrue(viewModel.uiState.value.resumeOptimization.isError)

        viewModel.toggleResumeSuggestion("suggestion-001")
        viewModel.applyResumeSuggestions()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.resumeOptimization.isError)
        assertTrue(viewModel.uiState.value.resumeOptimization.status.contains("1 条建议"))
    }

    @Test
    fun practicePresetProducesResultsAndEmptyState() = runTest {
        val viewModel = WorkspaceViewModel(InMemoryMockRepository(), mockDelayMillis = 0)

        viewModel.applyPracticePreset("basic")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.practice.results.isNotEmpty())

        viewModel.applyPracticePreset("empty")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.practice.results.isEmpty())
        assertTrue(viewModel.uiState.value.practice.submitted)
    }

    @Test
    fun recordsCanBeFilteredAndSelected() {
        val viewModel = WorkspaceViewModel(InMemoryMockRepository(), mockDelayMillis = 0)

        viewModel.updateRecordFilters(topic = "并发")
        viewModel.searchRecords()

        assertEquals(1, viewModel.uiState.value.records.records.size)
        assertEquals("record-003", viewModel.uiState.value.records.selectedId)
        assertEquals("failed", viewModel.uiState.value.records.selected?.summaryStatus)
    }

    @Test
    fun evaluationValidationAndRunProduceAPreviewRecord() = runTest {
        val viewModel = WorkspaceViewModel(InMemoryMockRepository(), mockDelayMillis = 0)

        viewModel.updateEvaluationForm(topK = "99")
        viewModel.runEvaluation()
        assertTrue(viewModel.uiState.value.evaluation.isError)

        viewModel.updateEvaluationForm(topK = "5", strategy = "text-balanced")
        viewModel.runEvaluation()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.evaluation.isError)
        assertEquals("eval-preview", viewModel.uiState.value.evaluation.selected?.id)
        assertEquals(3, viewModel.uiState.value.evaluation.runs.size)
    }
}
