package com.cscareerguide.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cscareerguide.app.data.RemoteCareerRepository
import com.cscareerguide.app.data.bridge.HttpCareerBridge
import com.cscareerguide.app.data.session.EncryptedAuthSessionStore
import com.cscareerguide.app.ui.AppViewModel
import com.cscareerguide.app.ui.AppViewModelFactory
import com.cscareerguide.app.ui.careerprep.CareerPrepViewModel
import com.cscareerguide.app.ui.careerprep.CareerPrepViewModelFactory
import com.cscareerguide.app.ui.navigation.CareerGuideApp
import com.cscareerguide.app.ui.planning.PlanningViewModel
import com.cscareerguide.app.ui.planning.PlanningViewModelFactory
import com.cscareerguide.app.ui.theme.CsCareerGuideTheme
import com.cscareerguide.app.ui.workspace.WorkspaceViewModel
import com.cscareerguide.app.ui.workspace.WorkspaceViewModelFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repository = remember {
                val sessionStore = EncryptedAuthSessionStore(applicationContext)
                val bridge = HttpCareerBridge(
                    apiBaseUrl = BuildConfig.API_BASE_URL,
                    sessionStore = sessionStore,
                    client = OkHttpClient.Builder()
                        .callTimeout(30, TimeUnit.SECONDS)
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build(),
                    allowCleartext = BuildConfig.ALLOW_CLEARTEXT,
                )
                RemoteCareerRepository(bridge, sessionStore)
            }
            val factory = remember(repository) { AppViewModelFactory(repository) }
            val planningFactory = remember(repository) { PlanningViewModelFactory(repository) }
            val careerPrepFactory = remember(repository) { CareerPrepViewModelFactory(repository) }
            val workspaceFactory = remember(repository) { WorkspaceViewModelFactory(repository) }
            val viewModel: AppViewModel = viewModel(factory = factory)
            val planningViewModel: PlanningViewModel = viewModel(factory = planningFactory)
            val careerPrepViewModel: CareerPrepViewModel = viewModel(factory = careerPrepFactory)
            val workspaceViewModel: WorkspaceViewModel = viewModel(factory = workspaceFactory)

            CsCareerGuideTheme {
                CareerGuideApp(
                    viewModel = viewModel,
                    planningViewModel = planningViewModel,
                    careerPrepViewModel = careerPrepViewModel,
                    workspaceViewModel = workspaceViewModel,
                )
            }
        }
    }
}
