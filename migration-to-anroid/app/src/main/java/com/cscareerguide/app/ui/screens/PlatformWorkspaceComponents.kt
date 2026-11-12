package com.cscareerguide.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.ui.navigation.Routes
import com.cscareerguide.app.ui.theme.CareerPalette

data class CourseModule(
    val route: String,
    val label: String,
)

val CourseModules = listOf(
    CourseModule(Routes.PLATFORM_CATALOG, "岗位与题库"),
    CourseModule(Routes.PLATFORM_RESUME, "简历优化"),
    CourseModule(Routes.PLATFORM_PRACTICE, "岗位题库练习"),
    CourseModule(Routes.PLATFORM_RECORDS, "练习记录"),
    CourseModule(Routes.PLATFORM_EVALUATION, "评估与调优（管理员）"),
)

@Composable
fun PlatformWorkspaceHeader(
    title: String,
    description: String,
    stageLabel: String,
    currentRoute: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CourseNav(
            currentRoute = currentRoute,
            onNavigate = onNavigate,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("route_back")) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = "课程项目 / 功能开发",
                    style = MaterialTheme.typography.labelLarge,
                    color = CareerPalette.Moss,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CareerPalette.WarningSoft,
                    border = BorderStroke(1.dp, CareerPalette.Warning.copy(alpha = 0.22f)),
                ) {
                    Text(
                        text = stageLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = CareerPalette.Warning,
                    )
                }
            }
        }
    }
}

@Composable
fun CourseNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("course_nav"),
    ) {
        CourseModules.forEach { module ->
            val active = module.route == currentRoute
            Surface(
                modifier = Modifier
                    .clickable { onNavigate(module.route) }
                    .testTag("course_${module.route.substringAfter("platform_")}"),
                shape = RoundedCornerShape(8.dp),
                color = if (active) CareerPalette.AccentSoft else CareerPalette.Surface,
                border = BorderStroke(
                    1.dp,
                    if (active) CareerPalette.Moss.copy(alpha = 0.35f) else CareerPalette.Border,
                ),
            ) {
                Text(
                    text = module.label,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) CareerPalette.Moss else CareerPalette.Muted,
                )
            }
        }
    }
}

@Composable
fun PlatformPage(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        content()
    }
}

@Composable
fun PlatformStatus(
    text: String,
    isError: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = if (isError) CareerPalette.ErrorSoft else CareerPalette.AccentSoft,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) CareerPalette.Error else CareerPalette.Moss,
            )
        }
    }
}
