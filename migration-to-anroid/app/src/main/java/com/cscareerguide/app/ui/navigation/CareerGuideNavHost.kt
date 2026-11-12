package com.cscareerguide.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cscareerguide.app.ui.AppUiState
import com.cscareerguide.app.ui.AppViewModel
import com.cscareerguide.app.ui.careerprep.CareerPrepViewModel
import com.cscareerguide.app.ui.careerprep.HuntingTab
import com.cscareerguide.app.ui.planning.PlanningViewModel
import com.cscareerguide.app.ui.screens.CatalogScreen
import com.cscareerguide.app.ui.screens.CareerEvaluationScreen
import com.cscareerguide.app.ui.screens.CareerPathDetailScreen
import com.cscareerguide.app.ui.screens.CareerPathsScreen
import com.cscareerguide.app.ui.screens.CareerPlanScreen
import com.cscareerguide.app.ui.screens.CompanyReviewsScreen
import com.cscareerguide.app.ui.screens.EvaluationScreen
import com.cscareerguide.app.ui.screens.HomeScreen
import com.cscareerguide.app.ui.screens.InterviewScreen
import com.cscareerguide.app.ui.screens.LoginScreen
import com.cscareerguide.app.ui.screens.PracticeRecordsScreen
import com.cscareerguide.app.ui.screens.PracticeScreen
import com.cscareerguide.app.ui.screens.ProfileScreen
import com.cscareerguide.app.ui.screens.RegisterScreen
import com.cscareerguide.app.ui.screens.ResumeScreen
import com.cscareerguide.app.ui.screens.ResumeOptimizationScreen
import com.cscareerguide.app.ui.screens.SelfIntroductionScreen
import com.cscareerguide.app.ui.screens.SeniorAdviceScreen
import com.cscareerguide.app.ui.theme.CareerPalette
import com.cscareerguide.app.ui.workspace.WorkspaceViewModel

@Composable
fun CareerGuideApp(
    viewModel: AppViewModel,
    planningViewModel: PlanningViewModel,
    careerPrepViewModel: CareerPrepViewModel,
    workspaceViewModel: WorkspaceViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (!uiState.isAuthenticated) {
        AuthenticationNavHost(
            uiState = uiState,
            viewModel = viewModel,
        )
    } else {
        MainNavHost(
            uiState = uiState,
            viewModel = viewModel,
            planningViewModel = planningViewModel,
            careerPrepViewModel = careerPrepViewModel,
            workspaceViewModel = workspaceViewModel,
        )
    }
}

@Composable
private fun AuthenticationNavHost(
    uiState: AppUiState,
    viewModel: AppViewModel,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                loading = uiState.authLoading,
                error = uiState.authError,
                onLogin = viewModel::login,
                onRegister = { navController.navigate(Routes.REGISTER) },
                onClearError = viewModel::clearAuthError,
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                loading = uiState.authLoading,
                error = uiState.authError,
                onRegister = viewModel::register,
                onBack = { navController.navigateUp() },
                onClearError = viewModel::clearAuthError,
            )
        }
    }
}

@Composable
private fun MainNavHost(
    uiState: AppUiState,
    viewModel: AppViewModel,
    planningViewModel: PlanningViewModel,
    careerPrepViewModel: CareerPrepViewModel,
    workspaceViewModel: WorkspaceViewModel,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val planningState by planningViewModel.uiState.collectAsStateWithLifecycle()
    val huntingState by careerPrepViewModel.huntingState.collectAsStateWithLifecycle()
    val resumeState by careerPrepViewModel.resumeState.collectAsStateWithLifecycle()
    val interviewState by careerPrepViewModel.interviewState.collectAsStateWithLifecycle()
    val workspaceState by workspaceViewModel.uiState.collectAsStateWithLifecycle()
    val currentRoute = backStackEntry?.destination?.route
    val selectedTab = MainTab.entries.firstOrNull { it.route == currentRoute }

    fun navigateTopLevel(tab: MainTab) {
        val activeRoute = navController.currentBackStackEntry?.destination?.route
        if (activeRoute == tab.route) return
        navController.navigate(tab.route) {
            popUpTo(Routes.HOME) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateGuarded(route: String) {
        val destination = RouteRegistry.safeDestination(route, uiState.isAuthenticated)
        navController.navigate(destination)
    }

    fun navigateCourse(route: String) {
        if (navController.currentBackStackEntry?.destination?.route == route) return
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("current_route_${currentRoute ?: "none"}"),
        containerColor = CareerPalette.Canvas,
        bottomBar = {
            if (selectedTab != null) {
                MainBottomBar(
                    selectedTab = selectedTab,
                    onSelect = ::navigateTopLevel,
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    user = uiState.user,
                    careerPaths = uiState.careerPaths,
                    metrics = uiState.homeMetrics,
                    onCareerPath = { type ->
                        navController.navigate(Routes.careerPathDetail(type))
                    },
                    onOpenTab = ::navigateTopLevel,
                )
            }

            composable(Routes.CAREER_PATHS) {
                CareerPathsScreen(
                    paths = planningState.careerPaths,
                    onOpenPath = { type ->
                        navController.navigate(Routes.careerPathDetail(type))
                    },
                )
            }

            composable(
                route = Routes.CAREER_PATH_DETAIL,
                arguments = listOf(navArgument("type") { type = NavType.StringType }),
            ) { entry ->
                val type = entry.arguments?.getString("type").orEmpty().ifBlank { "employment" }
                CareerPathDetailScreen(
                    path = planningState.careerPaths.firstOrNull { it.id == type },
                    type = type,
                    onBack = { navController.navigateUp() },
                    onStartIntroduction = {
                        navController.navigate(Routes.selfIntroduction(type))
                    },
                )
            }

            composable(
                route = Routes.SELF_INTRODUCTION,
                arguments = listOf(navArgument("type") { type = NavType.StringType }),
            ) { entry ->
                val type = entry.arguments?.getString("type").orEmpty().ifBlank { "employment" }
                SelfIntroductionScreen(
                    state = planningState,
                    onBack = { navController.navigateUp() },
                    onUpdate = planningViewModel::updateIntroduction,
                    onToggleSkill = planningViewModel::toggleSkill,
                    onSaveDraft = planningViewModel::saveDraftNow,
                    onNext = {
                        if (planningViewModel.finishIntroduction()) {
                            navController.navigate(Routes.CAREER_EVALUATION)
                        }
                    },
                )
            }

            composable(Routes.USERS) {
                ProfileScreen(
                    user = uiState.user,
                    introduction = planningState.introduction,
                    recommendations = planningState.recommendations,
                    resume = resumeState.draft,
                    interviewReview = interviewState.review,
                    recordCount = workspaceState.records.records.size,
                    onUpdateProfile = viewModel::updateProfile,
                    onNavigate = ::navigateGuarded,
                    onLogout = viewModel::logout,
                )
            }

            composable(Routes.CAREER_EVALUATION) {
                CareerEvaluationScreen(
                    state = planningState,
                    onBack = { navController.navigateUp() },
                    onEditIntroduction = {
                        navController.navigate(Routes.selfIntroduction("employment"))
                    },
                    onEnsureRecommendations = planningViewModel::ensureRecommendations,
                    onSelectCareer = planningViewModel::selectCareer,
                    onCreateReport = {
                        if (planningViewModel.createReport()) {
                            navController.navigate(Routes.CAREER_PLAN)
                        }
                    },
                )
            }

            composable(Routes.CAREER_PLAN) {
                CareerPlanScreen(
                    report = planningState.report,
                    onBack = { navController.navigateUp() },
                    onChooseCareer = {
                        navController.navigate(Routes.CAREER_EVALUATION)
                    },
                )
            }

            composable(Routes.COMPANY_REVIEWS) {
                CompanyReviewsScreen(
                    state = huntingState,
                    onSelectTab = careerPrepViewModel::selectHuntingTab,
                    onSearch = careerPrepViewModel::updateSearch,
                    onShareAdvice = {
                        careerPrepViewModel.resetAdviceForm()
                        navController.navigate(Routes.SENIOR_ADVICE_FORM)
                    },
                    onOpenResume = { navController.navigate(Routes.RESUME) },
                )
            }

            composable(Routes.SENIOR_ADVICE_FORM) {
                SeniorAdviceScreen(
                    state = huntingState.form,
                    onUpdate = careerPrepViewModel::updateAdviceForm,
                    onSubmit = careerPrepViewModel::submitAdvice,
                    onReset = careerPrepViewModel::resetAdviceForm,
                    onBack = {
                        careerPrepViewModel.selectHuntingTab(HuntingTab.ADVICE)
                        navController.navigateUp()
                    },
                )
            }

            composable(Routes.INTERVIEW) {
                InterviewScreen(
                    state = interviewState,
                    config = careerPrepViewModel.interviewConfig,
                    onSelectStyle = careerPrepViewModel::selectInterviewStyle,
                    onSelectDuration = careerPrepViewModel::selectInterviewDuration,
                    onToggleVoiceMock = careerPrepViewModel::toggleVoiceMock,
                    onStart = careerPrepViewModel::startInterview,
                    onSendAnswer = careerPrepViewModel::sendInterviewAnswer,
                    onPause = careerPrepViewModel::pauseInterview,
                    onFinish = careerPrepViewModel::finishInterview,
                    onReset = careerPrepViewModel::resetInterview,
                    onTick = careerPrepViewModel::tickInterviewClock,
                )
            }

            composable(Routes.RESUME) {
                ResumeScreen(
                    state = resumeState,
                    onBack = { navController.navigateUp() },
                    onSelectPane = careerPrepViewModel::selectResumePane,
                    onSelectSection = careerPrepViewModel::selectResumeSection,
                    onUpdateBasic = careerPrepViewModel::updateResumeBasic,
                    onUpdateEvaluation = careerPrepViewModel::updateSelfEvaluation,
                    onUpdateSkills = careerPrepViewModel::updateSkills,
                    onAddEducation = careerPrepViewModel::addEducation,
                    onUpdateEducation = careerPrepViewModel::updateEducation,
                    onDeleteEducation = careerPrepViewModel::deleteEducation,
                    onAddExperience = careerPrepViewModel::addExperience,
                    onUpdateExperience = careerPrepViewModel::updateExperience,
                    onDeleteExperience = careerPrepViewModel::deleteExperience,
                    onAddProject = careerPrepViewModel::addProject,
                    onUpdateProject = careerPrepViewModel::updateProject,
                    onDeleteProject = careerPrepViewModel::deleteProject,
                    onAddCertificate = careerPrepViewModel::addCertificate,
                    onUpdateCertificate = careerPrepViewModel::updateCertificate,
                    onDeleteCertificate = careerPrepViewModel::deleteCertificate,
                    onSave = careerPrepViewModel::saveResume,
                    onExport = careerPrepViewModel::exportResume,
                )
            }

            composable(Routes.PLATFORM_CATALOG) {
                CatalogScreen(
                    state = workspaceState.catalog,
                    currentRoute = Routes.PLATFORM_CATALOG,
                    onBack = { navController.navigateUp() },
                    onNavigate = ::navigateCourse,
                    onSelectKind = workspaceViewModel::selectCatalogKind,
                    onUpdateFilters = workspaceViewModel::updateCatalogFilters,
                    onSearch = workspaceViewModel::searchCatalog,
                    onReset = workspaceViewModel::resetCatalogFilters,
                )
            }

            composable(Routes.PLATFORM_RESUME) {
                ResumeOptimizationScreen(
                    state = workspaceState.resumeOptimization,
                    currentRoute = Routes.PLATFORM_RESUME,
                    onBack = { navController.navigateUp() },
                    onNavigate = ::navigateCourse,
                    onUpdateJd = workspaceViewModel::updateJdText,
                    onUpdateRevision = workspaceViewModel::updateResumeRevision,
                    onAnalyze = workspaceViewModel::analyzeResume,
                    onToggleSuggestion = workspaceViewModel::toggleResumeSuggestion,
                    onApply = workspaceViewModel::applyResumeSuggestions,
                )
            }

            composable(Routes.PLATFORM_PRACTICE) {
                PracticeScreen(
                    state = workspaceState.practice,
                    currentRoute = Routes.PLATFORM_PRACTICE,
                    onBack = { navController.navigateUp() },
                    onNavigate = ::navigateCourse,
                    onUpdateFilters = workspaceViewModel::updatePracticeFilters,
                    onRetrieve = workspaceViewModel::retrievePracticeQuestions,
                    onPreset = workspaceViewModel::applyPracticePreset,
                    onOpenInterview = { navigateCourse(Routes.INTERVIEW) },
                )
            }

            composable(Routes.PLATFORM_RECORDS) {
                PracticeRecordsScreen(
                    state = workspaceState.records,
                    currentRoute = Routes.PLATFORM_RECORDS,
                    onBack = { navController.navigateUp() },
                    onNavigate = ::navigateCourse,
                    onUpdateFilters = workspaceViewModel::updateRecordFilters,
                    onSearch = workspaceViewModel::searchRecords,
                    onSelectRecord = workspaceViewModel::selectRecord,
                )
            }

            composable(Routes.PLATFORM_EVALUATION) {
                EvaluationScreen(
                    state = workspaceState.evaluation,
                    currentRoute = Routes.PLATFORM_EVALUATION,
                    onBack = { navController.navigateUp() },
                    onNavigate = ::navigateCourse,
                    onUpdateForm = workspaceViewModel::updateEvaluationForm,
                    onRun = workspaceViewModel::runEvaluation,
                    onOpenRun = workspaceViewModel::openEvaluation,
                    onExport = workspaceViewModel::exportEvaluation,
                )
            }
        }
    }
}

@Composable
private fun MainBottomBar(
    selectedTab: MainTab,
    onSelect: (MainTab) -> Unit,
) {
    NavigationBar(
        modifier = Modifier.testTag("bottom_navigation"),
        containerColor = CareerPalette.Surface,
        tonalElevation = androidx.compose.ui.unit.Dp.Hairline,
    ) {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onSelect(tab) },
                modifier = Modifier.testTag("tab_${tab.name.lowercase()}"),
                icon = {
                    Icon(
                        imageVector = tabIcon(tab),
                        contentDescription = tab.label,
                    )
                },
                label = { androidx.compose.material3.Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CareerPalette.Accent,
                    selectedTextColor = CareerPalette.Accent,
                    indicatorColor = CareerPalette.AccentSoft,
                    unselectedIconColor = CareerPalette.Muted,
                    unselectedTextColor = CareerPalette.Muted,
                ),
            )
        }
    }
}

private fun tabIcon(tab: MainTab): ImageVector = when (tab) {
    MainTab.HOME -> Icons.Rounded.Home
    MainTab.PLAN -> Icons.Rounded.Explore
    MainTab.CAREER -> Icons.Rounded.Work
    MainTab.LEARN -> Icons.Rounded.PlayArrow
    MainTab.PROFILE -> Icons.Rounded.Person
}

