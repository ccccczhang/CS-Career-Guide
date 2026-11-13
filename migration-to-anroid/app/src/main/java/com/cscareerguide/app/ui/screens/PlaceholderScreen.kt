package com.cscareerguide.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.ui.theme.CareerPalette

data class RouteAction(
    val label: String,
    val route: String,
)

@Composable
fun PlaceholderScreen(
    title: String,
    webPath: String,
    description: String,
    actions: List<RouteAction> = emptyList(),
    showBack: Boolean,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CareerPalette.Canvas)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (showBack) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("route_back"),
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = webPath,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = CareerPalette.WarningSoft,
                border = BorderStroke(1.dp, CareerPalette.Warning.copy(alpha = 0.25f)),
            ) {
                Text(
                    text = "Phase 1",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = CareerPalette.Warning,
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = CareerPalette.Surface,
            border = BorderStroke(1.dp, CareerPalette.Border),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "页面骨架已接入",
                    style = MaterialTheme.typography.titleLarge,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
                Text(
                    text = "本阶段仅完成路由、导航和 Mock 架构验证。页面内容、真实数据和业务完成度会在后续阶段单独实现与验收。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Warning,
                )
            }
        }

        if (actions.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "继续进入",
                    style = MaterialTheme.typography.titleMedium,
                    color = CareerPalette.Ink,
                )
                actions.forEach { action ->
                    OutlinedButton(
                        onClick = { onNavigate(action.route) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("route_action_${action.route.replace('/', '_')}"),
                    ) {
                        Text(
                            text = action.label,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = null,
                        )
                    }
                }
            }
        }

        if (onLogout != null) {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_button"),
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("退出登录")
            }
        }
    }
}
