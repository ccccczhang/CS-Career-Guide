package com.cscareerguide.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.data.model.CareerPlanReport
import com.cscareerguide.app.ui.theme.CareerPalette

@Composable
fun CareerPlanScreen(
    report: CareerPlanReport?,
    onBack: () -> Unit,
    onChooseCareer: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CareerPalette.Canvas)
            .testTag("screen_career_plan"),
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
                text = "职业规划报告",
                style = MaterialTheme.typography.titleLarge,
                color = CareerPalette.Ink,
            )
        }

        if (report == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CareerPalette.WarningSoft,
                    border = BorderStroke(1.dp, Color(0xFFEADAB7)),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "还没有生成报告",
                            style = MaterialTheme.typography.titleLarge,
                            color = CareerPalette.Warning,
                        )
                        Text(
                            text = "请先在职业方向选择页确定一个目标岗位，再生成完整报告。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CareerPalette.Warning,
                        )
                        Button(
                            onClick = onChooseCareer,
                            colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
                        ) {
                            Text("选择职业方向")
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
            return
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = CareerPalette.Accent,
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "CAREER PLAN / MOCK",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFBCC8B5),
                        )
                        Text(
                            text = report.targetCareer,
                            style = MaterialTheme.typography.headlineMedium,
                            color = CareerPalette.Surface,
                        )
                        Text(
                            text = "本报告根据本地个人自述和目标岗位生成，仅用于 UI 与流程演示。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFDCE5D6),
                        )
                    }
                }
            }

            item {
                ReportSection("人才画像与匹配") {
                    ReportParagraph(report.talentPortrait)
                    ReportParagraph(report.personalityFit)
                    BulletList("优势", report.strengths, CareerPalette.Moss)
                    BulletList("待补强", report.weaknesses, CareerPalette.Warning)
                }
            }

            item {
                ReportSection("市场与城市建议") {
                    LabelValue("市场热度", report.marketHeat)
                    BulletList(
                        "薪资成长预测",
                        report.salaryForecast.map { "${it.year}：${it.range}（${it.condition}）" },
                        CareerPalette.Moss,
                    )
                    BulletList(
                        "城市建议",
                        report.citySuggestions.map {
                            "${it.city}（${it.fitScore} 分）：${it.reason}"
                        },
                        CareerPalette.Moss,
                    )
                }
            }

            item {
                ReportSection("至毕业成长路线") {
                    report.roadmap.forEach { phase ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF7F8F3),
                            border = BorderStroke(1.dp, CareerPalette.Border),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = phase.phase,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CareerPalette.Moss,
                                )
                                Text(
                                    text = phase.goal,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CareerPalette.Ink,
                                )
                                phase.actions.forEach { action ->
                                    ReportBullet(action)
                                }
                            }
                        }
                    }
                }
            }

            item {
                ReportSection("学习路径") {
                    report.learningPath.forEach { stage ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF7F8F3),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = stage.stage,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CareerPalette.Ink,
                                )
                                Text(
                                    text = stage.topics.joinToString(" · "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CareerPalette.Moss,
                                )
                                Text(
                                    text = "项目：${stage.project}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CareerPalette.Muted,
                                )
                            }
                        }
                    }
                }
            }

            item {
                ReportSection("求职策略与风险提醒") {
                    BulletList("求职策略", report.jobHuntingStrategy, CareerPalette.Moss)
                    BulletList("风险提醒", report.riskAlerts, CareerPalette.Error)
                }
            }

            item {
                ReportSection("近期计划") {
                    LabelValue("周重点", report.weeklyFocus)
                    report.weeklySchedule.forEach {
                        ReportParagraph("${it.day} · ${it.task}（${it.hours}h）")
                    }
                    Spacer(Modifier.height(4.dp))
                    LabelValue("月主题", report.monthlyTheme)
                    report.monthlyTargets.forEach { ReportBullet(it) }
                }
            }
        }
    }
}

@Composable
private fun ReportSection(
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
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
private fun ReportParagraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = CareerPalette.Muted,
    )
}

@Composable
private fun LabelValue(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "$label：",
            style = MaterialTheme.typography.bodyMedium,
            color = CareerPalette.Muted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = CareerPalette.Ink,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun BulletList(
    title: String,
    items: List<String>,
    color: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "•",
                    color = color,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = item,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
        }
    }
}

@Composable
private fun ReportBullet(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "•",
            color = CareerPalette.Moss,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = CareerPalette.Muted,
        )
    }
}
