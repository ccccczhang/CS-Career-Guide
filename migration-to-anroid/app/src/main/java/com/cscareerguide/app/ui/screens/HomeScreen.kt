package com.cscareerguide.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.data.model.CareerPathSummary
import com.cscareerguide.app.data.model.HomeMetrics
import com.cscareerguide.app.data.model.UserProfile
import com.cscareerguide.app.ui.navigation.MainTab
import com.cscareerguide.app.ui.theme.CareerPalette

@Composable
fun HomeScreen(
    user: UserProfile?,
    careerPaths: List<CareerPathSummary>,
    metrics: HomeMetrics,
    onCareerPath: (String) -> Unit,
    onOpenTab: (MainTab) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CareerPalette.Canvas)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .testTag("screen_home"),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "CS CAREER GUIDE",
                    style = MaterialTheme.typography.labelLarge,
                    color = CareerPalette.Moss,
                )
                Text(
                    text = "你好，${user?.displayName ?: "同学"}",
                    style = MaterialTheme.typography.titleLarge,
                    color = CareerPalette.Ink,
                )
            }
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onOpenTab(MainTab.PROFILE) }
                    .testTag("home_profile"),
                shape = CircleShape,
                color = CareerPalette.AccentSoft,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "个人中心",
                        tint = CareerPalette.Moss,
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = CareerPalette.Accent,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = "下一章，从清晰的方向开始",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CareerPalette.Surface,
                )
                Text(
                    text = "通过职业路径、个人自述和岗位选择，形成一份可以继续行动的规划。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFDDE4D7),
                )
                Surface(
                    modifier = Modifier
                        .clickable { onOpenTab(MainTab.PLAN) }
                        .testTag("home_start_plan"),
                    shape = RoundedCornerShape(10.dp),
                    color = CareerPalette.Surface,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "打开职业规划",
                            color = CareerPalette.Ink,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = CareerPalette.Ink,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Metric(label = "职业方向", value = "${metrics.careerPathCount} 类")
                    Metric(label = "匹配参考", value = "${metrics.recommendationRate}%")
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading(title = "职业大路径", subtitle = "先选择一条主线，再补充具体目标")
            careerPaths.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowItems.forEach { path ->
                        CareerPathCard(
                            path = path,
                            onClick = { onCareerPath(path.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading(title = "核心入口", subtitle = "按当前任务继续")
            QuickLink(
                title = "求职与企业",
                description = "职位、榜单、简历和前辈经验",
                onClick = { onOpenTab(MainTab.CAREER) },
            )
            QuickLink(
                title = "AI 模拟面试",
                description = "配置面试风格并进入 Mock 对话",
                onClick = { onOpenTab(MainTab.LEARN) },
            )
            QuickLink(
                title = "个人中心",
                description = "资料、复盘和下一步入口",
                onClick = { onOpenTab(MainTab.PROFILE) },
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = CareerPalette.WarningSoft,
            border = BorderStroke(1.dp, Color(0xFFEADAB7)),
        ) {
            Text(
                text = "UI/Mock · 当前页面使用本地示例数据，不代表真实账号、推荐或招聘结果。",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Warning,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = value,
            color = CareerPalette.Surface,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = label,
            color = Color(0xFFBCC8B5),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun CareerPathCard(
    path: CareerPathSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .aspectRatio(0.82f)
            .clickable(onClick = onClick)
            .testTag("career_path_${path.id}"),
        shape = RoundedCornerShape(16.dp),
        color = CareerPalette.Surface,
        border = BorderStroke(1.dp, CareerPalette.Border),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CareerPalette.AccentSoft,
            ) {
                Text(
                    text = path.mark,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = CareerPalette.Moss,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = path.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = path.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "查看${path.title}",
                tint = CareerPalette.Moss,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun QuickLink(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = CareerPalette.Surface,
        border = BorderStroke(1.dp, CareerPalette.Border),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = CareerPalette.AccentSoft,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (title.contains("面试")) {
                            Icons.Rounded.Explore
                        } else {
                            Icons.Rounded.School
                        },
                        contentDescription = null,
                        tint = CareerPalette.Moss,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = CareerPalette.Muted,
            )
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = CareerPalette.Ink,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = CareerPalette.Muted,
        )
    }
}
