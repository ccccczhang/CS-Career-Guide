package com.cscareerguide.app.data

import com.cscareerguide.app.data.bridge.BridgeError
import com.cscareerguide.app.data.bridge.PracticeRetrieveRequest
import com.cscareerguide.app.data.model.CareerPathSummary
import com.cscareerguide.app.data.model.CareerCategory
import com.cscareerguide.app.data.model.CareerPlanReport
import com.cscareerguide.app.data.model.CatalogItem
import com.cscareerguide.app.data.model.CompanyRanking
import com.cscareerguide.app.data.model.EvaluationRun
import com.cscareerguide.app.data.model.HomeMetrics
import com.cscareerguide.app.data.model.IntroductionDraft
import com.cscareerguide.app.data.model.InterviewConfig
import com.cscareerguide.app.data.model.InterviewReview
import com.cscareerguide.app.data.model.JobSummary
import com.cscareerguide.app.data.model.PracticeQuestion
import com.cscareerguide.app.data.model.PracticeRetrieval
import com.cscareerguide.app.data.model.PracticeRecordDetail
import com.cscareerguide.app.data.model.Recommendation
import com.cscareerguide.app.data.model.ResumeAnalysis
import com.cscareerguide.app.data.model.ResumeDraft
import com.cscareerguide.app.data.model.SeniorAdvice
import com.cscareerguide.app.data.model.SeniorAdviceDraft
import com.cscareerguide.app.data.model.SkillGroup
import com.cscareerguide.app.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

sealed interface AuthOutcome {
    data class Success(val user: UserProfile) : AuthOutcome
    data class Failure(
        val message: String,
        val error: BridgeError? = null,
    ) : AuthOutcome
}

interface AppRepository {
    val authInvalidations: Flow<Unit>
        get() = emptyFlow()

    fun currentUser(): UserProfile?
    fun careerPaths(): List<CareerPathSummary>
    fun homeMetrics(): HomeMetrics
    fun skillGroups(): List<SkillGroup>
    fun careerCategories(): List<CareerCategory>
    fun introductionDraft(): IntroductionDraft
    fun saveIntroduction(draft: IntroductionDraft)
    fun recommendations(draft: IntroductionDraft): List<Recommendation>
    fun careerPlan(draft: IntroductionDraft, targetCareer: String): CareerPlanReport
    fun jobs(): List<JobSummary>
    fun companyRankings(isPositive: Boolean): List<CompanyRanking>
    fun seniorAdvice(): List<SeniorAdvice>
    fun addSeniorAdvice(draft: SeniorAdviceDraft): SeniorAdvice
    fun resumeDraft(): ResumeDraft
    fun saveResume(draft: ResumeDraft)
    fun interviewConfig(): InterviewConfig
    fun interviewReview(style: String): InterviewReview
    fun catalogItems(): List<CatalogItem>
    fun resumeAnalysis(revision: Int): ResumeAnalysis
    fun practiceQuestions(): List<PracticeQuestion>
    fun practiceRecords(): List<PracticeRecordDetail>
    fun evaluationRuns(): List<EvaluationRun>
    fun evaluationRun(datasetVersion: String, strategy: String, topK: Int): EvaluationRun
    suspend fun loadResume(): AppResult<ResumeDraft?>
    suspend fun saveResumeRemote(draft: ResumeDraft): AppResult<ResumeDraft>
    suspend fun loadCatalog(query: CatalogQuery): AppResult<PagedData<CatalogItem>>
    suspend fun loadJobs(search: String, page: Int, pageSize: Int): AppResult<PagedData<JobSummary>>
    suspend fun loadSeniorAdvice(
        search: String,
        ordering: String,
        page: Int,
        pageSize: Int,
    ): AppResult<PagedData<SeniorAdvice>>
    suspend fun retrievePracticeQuestions(
        request: PracticeRetrieveRequest,
    ): AppResult<PracticeRetrieval>
    suspend fun analyzeResumeRemote(jdText: String, revision: Int, requestId: String): AppResult<Unit>
    suspend fun applyResumeAnalysisRemote(
        analysisId: String,
        baseRevision: Int,
        suggestionIds: List<String>,
        requestId: String,
    ): AppResult<Unit>
    suspend fun updateProfile(user: UserProfile): AuthOutcome
    suspend fun restoreSession(): AuthOutcome?
    suspend fun login(username: String, password: String): AuthOutcome
    suspend fun register(username: String, password: String, confirmation: String): AuthOutcome
    suspend fun logout()
}
