package com.cscareerguide.app.ui.navigation

enum class MainTab(
    val label: String,
    val route: String,
) {
    HOME("首页", Routes.HOME),
    PLAN("规划", Routes.CAREER_PATHS),
    CAREER("求职", Routes.COMPANY_REVIEWS),
    LEARN("学习", Routes.INTERVIEW),
    PROFILE("我的", Routes.USERS),
}

data class RouteSpec(
    val navRoute: String,
    val webPath: String,
    val title: String,
    val tab: MainTab?,
)

object Routes {
    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CAREER_PATHS = "career_paths"
    const val CAREER_PATH_DETAIL = "career_path_detail/{type}"
    const val SELF_INTRODUCTION = "self_introduction/{type}"
    const val USERS = "users"
    const val CAREER_EVALUATION = "career_evaluation"
    const val CAREER_PLAN = "career_plan"
    const val COMPANY_REVIEWS = "company_reviews"
    const val SENIOR_ADVICE_FORM = "senior_advice_form"
    const val INTERVIEW = "interview"
    const val RESUME = "resume"
    const val PLATFORM_CATALOG = "platform_catalog"
    const val PLATFORM_RESUME = "platform_resume"
    const val PLATFORM_PRACTICE = "platform_practice"
    const val PLATFORM_RECORDS = "platform_records"
    const val PLATFORM_EVALUATION = "platform_evaluation"

    fun careerPathDetail(type: String): String = "career_path_detail/$type"

    fun selfIntroduction(type: String): String = "self_introduction/$type"
}

object RouteRegistry {
    val allRoutes: List<RouteSpec> = listOf(
        RouteSpec(Routes.HOME, "/", "首页", MainTab.HOME),
        RouteSpec(Routes.LOGIN, "/login", "登录", null),
        RouteSpec(Routes.REGISTER, "/register", "注册", null),
        RouteSpec(Routes.CAREER_PATHS, "/career-paths", "职业路径", MainTab.PLAN),
        RouteSpec(Routes.CAREER_PATH_DETAIL, "/career-paths/:type", "职业路径详情", MainTab.PLAN),
        RouteSpec(Routes.SELF_INTRODUCTION, "/career-paths/:type/self-introduction", "个人自述", MainTab.PLAN),
        RouteSpec(Routes.USERS, "/users", "个人中心", MainTab.PROFILE),
        RouteSpec(Routes.CAREER_EVALUATION, "/career-evaluation", "职业方向选择", MainTab.PLAN),
        RouteSpec(Routes.CAREER_PLAN, "/career-plan", "规划报告", MainTab.PLAN),
        RouteSpec(Routes.COMPANY_REVIEWS, "/company-reviews", "企业与职位", MainTab.CAREER),
        RouteSpec(Routes.SENIOR_ADVICE_FORM, "/company-reviews/senior-advice/form", "分享经验", MainTab.CAREER),
        RouteSpec(Routes.INTERVIEW, "/interview", "AI 模拟面试", MainTab.LEARN),
        RouteSpec(Routes.RESUME, "/resume", "简历工作台", MainTab.CAREER),
        RouteSpec(Routes.PLATFORM_CATALOG, "/career-platform/catalog", "岗位与题库", MainTab.LEARN),
        RouteSpec(Routes.PLATFORM_RESUME, "/career-platform/resume", "简历优化", MainTab.LEARN),
        RouteSpec(Routes.PLATFORM_PRACTICE, "/career-platform/practice", "岗位题库练习", MainTab.LEARN),
        RouteSpec(Routes.PLATFORM_RECORDS, "/career-platform/records", "练习记录", MainTab.LEARN),
        RouteSpec(Routes.PLATFORM_EVALUATION, "/career-platform/evaluation", "评估与调优", MainTab.PROFILE),
    )

    val publicRoutes: Set<String> = setOf(Routes.LOGIN, Routes.REGISTER)
    val protectedRoutes: Set<String> = allRoutes
        .asSequence()
        .map(RouteSpec::navRoute)
        .filterNot(publicRoutes::contains)
        .toSet()

    fun initialDestination(isAuthenticated: Boolean): String =
        if (isAuthenticated) Routes.HOME else Routes.LOGIN

    fun safeDestination(target: String?, isAuthenticated: Boolean): String {
        if (target.isNullOrBlank()) return initialDestination(isAuthenticated)
        if (!isAuthenticated && target in protectedRoutes) return Routes.LOGIN
        return target
    }
}
