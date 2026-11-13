package com.cscareerguide.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.data.model.CareerPathSummary
import com.cscareerguide.app.ui.theme.CareerPalette

@Composable
fun CareerPathsScreen(
    paths: List<CareerPathSummary>,
    onOpenPath: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CareerPalette.Canvas)
            .testTag("screen_career_paths"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            top = 22.dp,
            end = 16.dp,
            bottom = 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "PLAN / CAREER PATHS",
                    style = MaterialTheme.typography.labelLarge,
                    color = CareerPalette.Moss,
                )
                Text(
                    text = "先选一条职业主线",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = "职业路径不是最终答案，而是帮助你组织信息、技能和下一步行动。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
        }

        items(paths.size) { index ->
            val path = paths[index]
            CareerPathLargeCard(
                path = path,
                onClick = { onOpenPath(path.id) },
            )
        }
    }
}

@Composable
fun CareerPathDetailScreen(
    path: CareerPathSummary?,
    type: String,
    onBack: () -> Unit,
    onStartIntroduction: () -> Unit,
) {
    val resolved = path ?: CareerPathSummary(
        id = type,
        title = "职业路径",
        subtitle = "探索下一步",
        description = "当前路径使用本地示例内容。",
        mark = "00",
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CareerPalette.Canvas)
            .testTag("screen_career_path_detail"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, top = 10.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "${resolved.title}路径",
                style = MaterialTheme.typography.titleLarge,
                color = CareerPalette.Ink,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = CareerPalette.Accent,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = resolved.mark,
                        color = CareerPalette.WarningSoft,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = resolved.title,
                        color = CareerPalette.Surface,
                        style = MaterialTheme.typography.displaySmall,
                    )
                    Text(
                        text = resolved.subtitle,
                        color = androidx.compose.ui.graphics.Color(0xFFDCE5D6),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = resolved.description,
                        color = androidx.compose.ui.graphics.Color(0xFFDCE5D6),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            PlanningSection(title = "准备建议") {
                pathSuggestions(type).forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = (index + 1).toString().padStart(2, '0'),
                            color = CareerPalette.Moss,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = item,
                            color = CareerPalette.Muted,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            PlanningSection(title = "可继续探索") {
                Text(
                    text = "完成个人自述后，系统会根据技能、经历和职业目标生成本地 Mock 推荐，再选择具体岗位生成规划报告。",
                    color = CareerPalette.Muted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CareerPalette.Surface,
            tonalElevation = 3.dp,
        ) {
            Button(
                onClick = onStartIntroduction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp)
                    .testTag("start_introduction"),
                colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
            ) {
                Text("开始个人自述")
                Spacer(Modifier.size(8.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun PlanningSection(
    title: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CareerPalette.Surface,
        border = BorderStroke(1.dp, CareerPalette.Border),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = CareerPalette.Ink,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun CareerPathLargeCard(
    path: CareerPathSummary,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("career_path_card_${path.id}"),
        shape = RoundedCornerShape(18.dp),
        color = CareerPalette.Surface,
        border = BorderStroke(1.dp, CareerPalette.Border),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(16.dp),
                color = CareerPalette.AccentSoft,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = path.mark,
                        color = CareerPalette.Moss,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
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
                Text(
                    text = path.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = CareerPalette.Moss,
            )
        }
    }
}

private fun pathSuggestions(type: String): List<String> = when (type) {
    "postgraduate" -> listOf(
        "核对目标院校、专业方向和考试科目。",
        "用真题结果调整复习节奏，而不是只看学习时长。",
        "整理项目、竞赛和科研经历，准备复试表达。",
    )

    "civil_service" -> listOf(
        "核对岗位专业、学历和应届身份限制。",
        "按行测、申论和面试拆分阶段目标。",
        "持续关注官方招录信息，保留报名材料。",
    )

    "entrepreneurship" -> listOf(
        "先验证真实问题，再投入完整产品开发。",
        "用小规模用户反馈检验方案。",
        "提前了解合同、知识产权和资金风险。",
    )

    else -> listOf(
        "补齐数据结构、算法和计算机基础课程。",
        "完成一个能够演示、部署和复盘的项目。",
        "对照目标岗位准备简历、面试和投递记录。",
    )
}
