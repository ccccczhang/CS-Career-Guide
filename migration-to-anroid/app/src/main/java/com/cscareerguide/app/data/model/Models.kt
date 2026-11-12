package com.cscareerguide.app.data.model

data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String,
    val school: String,
    val major: String,
    val grade: String,
    val profile: String,
    val education: String = "本科",
    val email: String = "",
    val address: String = "",
)

data class CareerPathSummary(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val mark: String,
)

data class HomeMetrics(
    val careerPathCount: Int,
    val recommendationRate: Int,
)

data class SkillOption(
    val value: String,
    val label: String,
)

data class SkillGroup(
    val id: String,
    val title: String,
    val description: String,
    val skills: List<SkillOption>,
)

data class CareerCategory(
    val id: String,
    val title: String,
    val roles: List<String>,
)

data class Recommendation(
    val career: String,
    val matchScore: Int,
    val reason: String,
)

data class IntroductionDraft(
    val name: String = "",
    val school: String = "",
    val major: String = "",
    val grade: String = "",
    val goal: String = "",
    val otherSkills: String = "",
    val selfDescription: String = "",
    val skills: Set<String> = emptySet(),
) {
    val completedItems: Int
        get() = listOf(name, school, major, grade, goal, otherSkills, selfDescription)
            .count(String::isNotBlank) + if (skills.isNotEmpty()) 1 else 0

    val progress: Int
        get() = (completedItems * 100 / 8).coerceIn(0, 100)
}

data class SalaryForecast(
    val year: String,
    val range: String,
    val condition: String,
)

data class CitySuggestion(
    val city: String,
    val fitScore: Int,
    val reason: String,
)

data class RoadmapPhase(
    val phase: String,
    val goal: String,
    val actions: List<String>,
)

data class LearningStage(
    val stage: String,
    val topics: List<String>,
    val project: String,
)

data class WeeklyTask(
    val day: String,
    val task: String,
    val hours: Int,
)

data class CareerPlanReport(
    val targetCareer: String,
    val talentPortrait: String,
    val personalityFit: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val marketHeat: String,
    val salaryForecast: List<SalaryForecast>,
    val citySuggestions: List<CitySuggestion>,
    val roadmap: List<RoadmapPhase>,
    val learningPath: List<LearningStage>,
    val jobHuntingStrategy: List<String>,
    val riskAlerts: List<String>,
    val weeklyFocus: String,
    val weeklySchedule: List<WeeklyTask>,
    val monthlyTheme: String,
    val monthlyTargets: List<String>,
)

data class JobSummary(
    val id: String,
    val title: String,
    val company: String,
    val city: String,
    val salary: String,
    val industry: String = "",
    val companySize: String = "",
    val description: String = "",
    val companyDescription: String = "",
)

data class CompanyRanking(
    val id: String,
    val rank: Int,
    val title: String,
    val company: String,
    val city: String,
    val salary: String = "",
    val reason: String,
    val isPositive: Boolean,
)

data class SeniorAdvice(
    val id: String,
    val company: String,
    val position: String,
    val author: String,
    val graduationYear: String = "",
    val rating: Double,
    val content: String,
    val currentCompany: String = "",
    val salaryInfo: String = "",
    val pros: List<String> = emptyList(),
    val cons: List<String> = emptyList(),
)

data class SeniorAdviceDraft(
    val company: String = "",
    val position: String = "",
    val author: String = "",
    val graduationYear: String = "",
    val currentCompany: String = "",
    val advice: String = "",
    val salaryInfo: String = "",
    val rating: String = "4",
    val pros: String = "",
    val cons: String = "",
)

data class ResumeEducation(
    val school: String = "",
    val major: String = "",
    val degree: String = "",
    val period: String = "",
    val description: String = "",
)

data class ResumeExperience(
    val company: String = "",
    val position: String = "",
    val period: String = "",
    val description: String = "",
)

data class ResumeProject(
    val name: String = "",
    val role: String = "",
    val period: String = "",
    val url: String = "",
    val description: String = "",
)

data class ResumeCertificate(
    val name: String = "",
    val issuer: String = "",
    val period: String = "",
    val description: String = "",
)

data class ResumeDraft(
    val name: String = "",
    val targetPosition: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val selfEvaluation: String = "",
    val skills: String = "",
    val education: List<ResumeEducation> = emptyList(),
    val experience: List<ResumeExperience> = emptyList(),
    val projects: List<ResumeProject> = emptyList(),
    val certificates: List<ResumeCertificate> = emptyList(),
)

data class InterviewStyleOption(
    val id: String,
    val title: String,
    val description: String,
)

data class InterviewConfig(
    val styles: List<InterviewStyleOption>,
    val durationsMinutes: List<Int>,
)

enum class InterviewSpeaker {
    INTERVIEWER,
    USER,
}

data class InterviewMessage(
    val id: String,
    val speaker: InterviewSpeaker,
    val content: String,
)

data class InterviewReview(
    val id: String,
    val style: String,
    val score: Int,
    val summary: String,
    val strengths: List<String>,
    val improvements: List<String>,
    val nextActions: List<String>,
)

data class CourseItem(
    val id: String,
    val title: String,
    val lecturer: String,
    val category: String,
)

data class PracticeRecord(
    val id: String,
    val createdAt: String,
    val type: String,
    val score: Int?,
    val summary: String,
)
