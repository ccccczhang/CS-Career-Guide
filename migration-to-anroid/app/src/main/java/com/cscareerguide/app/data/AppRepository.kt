package com.cscareerguide.app.data

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
import com.cscareerguide.app.data.model.PracticeRecordDetail
import com.cscareerguide.app.data.model.Recommendation
import com.cscareerguide.app.data.model.ResumeAnalysis
import com.cscareerguide.app.data.model.ResumeDraft
import com.cscareerguide.app.data.model.SeniorAdvice
import com.cscareerguide.app.data.model.SeniorAdviceDraft
import com.cscareerguide.app.data.model.SkillGroup
import com.cscareerguide.app.data.model.UserProfile

sealed interface AuthOutcome {
    data class Success(val user: UserProfile) : AuthOutcome
    data class Failure(val message: String) : AuthOutcome
}

interface AppRepository {
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
    fun updateProfile(user: UserProfile): UserProfile
    suspend fun login(username: String, password: String): AuthOutcome
    suspend fun register(username: String, password: String, confirmation: String): AuthOutcome
    fun logout()
}
