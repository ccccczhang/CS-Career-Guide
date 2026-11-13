package com.cscareerguide.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.data.model.PracticeQuestion
import com.cscareerguide.app.ui.theme.CareerPalette
import com.cscareerguide.app.ui.workspace.PracticeUiState

@Composable
fun PracticeScreen(
    state: PracticeUiState,
    currentRoute: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onUpdateFilters: (String?, String?, String?, String?, Int?) -> Unit,
    onRetrieve: () -> Unit,
    onPreset: (String) -> Unit,
    onOpenInterview: () -> Unit,
) {
    var expandedEvidence by rememberSaveable { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .testTag("screen_platform_practice"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PlatformWorkspaceHeader(
            title = "让每一道题，都有据可查",
            description = "选择岗位、知识点和难度，从正式题库检索候选题，并查看逐字段来源证据。",
            stageLabel = "正式题库 · 文本检索",
            currentRoute = currentRoute,
            onBack = onBack,
            onNavigate = onNavigate,
        )
        MockStatusBanner("当前只检索题目和来源证据；不调用模型，也不创建练习会话。")
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenInterview),
            shape = RoundedCornerShape(10.dp),
            color = CareerPalette.AccentSoft,
        ) {
            Text(
                text = "想直接练习对话？进入自由模拟面试 →",
                modifier = Modifier.padding(13.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Moss,
            )
        }

        PrepSectionCard {
            SectionLabel("检索条件", "结果只用于比较文本相关程度")
            PlatformPracticeInput("岗位方向", state.role) {
                onUpdateFilters(it, null, null, null, null)
            }
            PlatformPracticeInput("知识点", state.topic) {
                onUpdateFilters(null, it, null, null, null)
            }
            PlatformPracticeInput("补充 JD（可选）", state.jdText) {
                onUpdateFilters(null, null, null, it, null)
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("easy" to "基础", "medium" to "进阶", "hard" to "深入")
                    .forEach { (value, label) ->
                        Surface(
                            modifier = Modifier
                                .clickable {
                                    onUpdateFilters(null, null, value, null, null)
                                }
                                .testTag("practice_difficulty_$value"),
                            shape = RoundedCornerShape(50),
                            color = if (state.difficulty == value) {
                                CareerPalette.Accent
                            } else {
                                CareerPalette.Canvas
                            },
                            border = BorderStroke(1.dp, CareerPalette.Border),
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (state.difficulty == value) {
                                    CareerPalette.Surface
                                } else {
                                    CareerPalette.Muted
                                },
                            )
                        }
                    }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 3, 5).forEach { count ->
                    Surface(
                        modifier = Modifier.clickable {
                            onUpdateFilters(null, null, null, null, count)
                        },
                        shape = RoundedCornerShape(50),
                        color = if (state.topK == count) CareerPalette.AccentSoft else CareerPalette.Canvas,
                    ) {
                        Text(
                            text = "$count 道",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = CareerPalette.Moss,
                        )
                    }
                }
            }
            Button(
                onClick = onRetrieve,
                enabled = !state.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("practice_retrieve"),
                colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
            ) {
                Text(if (state.loading) "正在检索…" else "检索候选题")
            }
            Text(
                text = "试一试",
                style = MaterialTheme.typography.labelLarge,
                color = CareerPalette.Muted,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "basic" to "集合基础题",
                    "rank" to "ArrayList 排序",
                    "empty" to "不存在的知识点",
                ).forEach { (key, label) ->
                    Surface(
                        modifier = Modifier
                            .clickable { onPreset(key) }
                            .testTag("practice_preset_$key"),
                        shape = RoundedCornerShape(8.dp),
                        color = CareerPalette.Surface,
                        border = BorderStroke(1.dp, CareerPalette.Border),
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CareerPalette.Moss,
                        )
                    }
                }
            }
        }

        PlatformStatus(state.status, isError = state.isError)
        if (state.submitted) {
            Text(
                text = "本次条件：${state.role} / ${state.topic.ifBlank { "全部知识点" }} / " +
                    "${state.difficulty.ifBlank { "全部难度" }} · ${state.jdText.ifBlank { "未提供 JD" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Muted,
            )
        }

        if (state.submitted && !state.loading && state.results.isEmpty()) {
            EmptyPrepState(
                title = "没有符合条件的题目",
                description = "尝试其他岗位、知识点或清空 JD；这里不会补入不相关题目。",
            )
        } else {
            state.results.forEachIndexed { index, item ->
                PracticeQuestionCard(
                    index = index,
                    item = item,
                    evidenceExpanded = expandedEvidence == item.id,
                    onToggleEvidence = {
                        expandedEvidence = if (expandedEvidence == item.id) null else item.id
                    },
                )
            }
        }
        Text(
            text = "匹配值只用于比较文本相关程度，不是答案正确率或能力评分。",
            style = MaterialTheme.typography.bodyMedium,
            color = CareerPalette.Muted,
        )
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun PracticeQuestionCard(
    index: Int,
    item: PracticeQuestion,
    evidenceExpanded: Boolean,
    onToggleEvidence: () -> Unit,
) {
    PrepSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "候选 ${index + 1} · ${item.origin.ifBlank { "正式题库" }}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = CareerPalette.Muted,
            )
            Tag("匹配 %.3f".format(item.matchScore), CareerPalette.Moss)
        }
        Text(
            text = item.question,
            style = MaterialTheme.typography.titleMedium,
            color = CareerPalette.Ink,
        )
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = CareerPalette.AccentSoft,
        ) {
            Column(
                modifier = Modifier.padding(13.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "参考要点",
                    style = MaterialTheme.typography.labelLarge,
                    color = CareerPalette.Moss,
                )
                item.keyPoints.forEach { point ->
                    Text(
                        text = "• $point",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CareerPalette.Ink,
                    )
                }
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleEvidence),
            shape = RoundedCornerShape(10.dp),
            color = CareerPalette.Surface,
            border = BorderStroke(1.dp, CareerPalette.Border),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = if (evidenceExpanded) "收起来源证据" else "查看来源证据",
                    style = MaterialTheme.typography.labelLarge,
                    color = CareerPalette.Moss,
                )
                if (evidenceExpanded) {
                    val evidenceLines = if (item.evidence.isNotEmpty()) {
                        item.evidence.map {
                            "${it.fieldPath}: ${it.quote} · 来源 ${it.sourceId}"
                        }
                    } else {
                        item.sourceEvidence
                    }
                    evidenceLines.forEach { evidence ->
                        Text(
                            text = evidence,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CareerPalette.Muted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformPracticeInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
    )
}
