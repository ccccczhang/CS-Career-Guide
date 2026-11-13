package com.cscareerguide.app.data.model

enum class CatalogKind(val label: String) {
    JOBS("岗位"),
    COMPANIES("企业"),
    QUESTIONS("题库"),
}

data class CatalogItem(
    val id: String,
    val kind: CatalogKind,
    val title: String,
    val subtitle: String,
    val city: String = "",
    val role: String = "",
    val topic: String = "",
    val difficulty: String = "",
    val tags: List<String> = emptyList(),
    val description: String = "",
    val keyPoints: List<String> = emptyList(),
    val source: String,
    val sourceUrl: String = "",
    val collectedAt: String,
    val permissionStatus: String = "allowed",
    val isDemo: Boolean = true,
)

data class ResumeSuggestion(
    val id: String,
    val section: String,
    val original: String,
    val evidence: String,
    val suggestion: String,
)

data class ResumeAnalysis(
    val id: String,
    val resumeRevision: Int,
    val summary: String,
    val suggestions: List<ResumeSuggestion>,
)

data class PracticeQuestion(
    val id: String,
    val question: String,
    val role: String,
    val topic: String,
    val difficulty: String,
    val keyPoints: List<String>,
    val sourceEvidence: List<String>,
    val matchScore: Double,
    val evidence: List<QuestionEvidence> = emptyList(),
    val origin: String = "",
    val source: String = "",
    val sourceUrl: String = "",
    val permissionStatus: String = "allowed",
    val collectedAt: String = "",
)

data class QuestionEvidence(
    val sourceId: String,
    val fieldPath: String,
    val quote: String,
)

data class PracticeRetrieval(
    val count: Int,
    val retrievalId: String,
    val message: String,
    val results: List<PracticeQuestion>,
)

data class PracticeAnswer(
    val id: String,
    val question: String,
    val answer: String,
    val coveredPoints: List<String>,
    val missingPoints: List<String>,
    val followUp: String,
)

data class PracticeRecordDetail(
    val id: String,
    val title: String,
    val role: String,
    val topic: String,
    val difficulty: String,
    val status: String,
    val createdAt: String,
    val summaryStatus: String,
    val strengths: List<String>,
    val gaps: List<String>,
    val nextSteps: List<String>,
    val answers: List<PracticeAnswer>,
)

data class EvaluationMetrics(
    val precisionAtK: Double,
    val recallAtK: Double,
    val f1AtK: Double,
    val hitRateAtK: Double,
    val mrr: Double,
)

data class EvaluationRun(
    val id: String,
    val task: String,
    val datasetVersion: String,
    val strategy: String,
    val topK: Int,
    val createdAt: String,
    val sampleCount: Int,
    val failureCount: Int,
    val modelVersion: String,
    val metrics: EvaluationMetrics,
)
