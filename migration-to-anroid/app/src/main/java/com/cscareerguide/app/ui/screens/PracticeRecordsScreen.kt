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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.data.model.PracticeAnswer
import com.cscareerguide.app.data.model.PracticeRecordDetail
import com.cscareerguide.app.ui.theme.CareerPalette
import com.cscareerguide.app.ui.workspace.RecordsUiState

@Composable
fun PracticeRecordsScreen(
    state: RecordsUiState,
    currentRoute: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onUpdateFilters: (String?, String?, String?, String?) -> Unit,
    onSearch: () -> Unit,
    onSelectRecord: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .testTag("screen_platform_records"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PlatformWorkspaceHeader(
            title = "练习记录",
            description = "查看自己的题目、真实回答、逐题反馈与结束总结。",
            stageLabel = "练习历史 · Mock",
            currentRoute = currentRoute,
            onBack = onBack,
            onNavigate = onNavigate,
        )
        MockStatusBanner("记录、反馈与总结均为本地示例；桥接后按当前 Token 隔离用户数据。")

        PrepSectionCard {
            SectionLabel("筛选记录", "无结果时不会用推荐记录补齐")
            RecordInput("关键词", state.query) {
                onUpdateFilters(it, null, null, null)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RecordInput("岗位方向", state.role, Modifier.weight(1f)) {
                    onUpdateFilters(null, it, null, null)
                }
                RecordInput("知识点", state.topic, Modifier.weight(1f)) {
                    onUpdateFilters(null, null, it, null)
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("" to "全部", "easy" to "基础", "medium" to "进阶", "hard" to "深入")
                    .forEach { (value, label) ->
                        Surface(
                            modifier = Modifier.clickable {
                                onUpdateFilters(null, null, null, value)
                            },
                            shape = RoundedCornerShape(50),
                            color = if (state.difficulty == value) {
                                CareerPalette.AccentSoft
                            } else {
                                CareerPalette.Canvas
                            },
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = CareerPalette.Moss,
                            )
                        }
                    }
            }
            Button(
                onClick = onSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("records_search"),
                colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
            ) {
                Text("查询记录")
            }
        }

        PlatformStatus(state.status, isError = false)
        SectionLabel("历史列表", "${state.records.size} 条")
        if (state.records.isEmpty()) {
            EmptyPrepState(
                title = "没有符合条件的练习记录",
                description = "调整筛选条件，或完成一次新的题库练习。",
            )
        } else {
            state.records.forEach { record ->
                RecordCard(
                    record = record,
                    selected = record.id == state.selectedId,
                    onClick = { onSelectRecord(record.id) },
                )
            }
        }

        val detail = state.selected
        if (detail != null) {
            RecordDetail(detail)
        } else {
            EmptyPrepState(
                title = "选择一条记录",
                description = "点击历史列表查看回答、逐题反馈与结束总结。",
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun RecordCard(
    record: PracticeRecordDetail,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("record_${record.id}"),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) CareerPalette.AccentSoft else CareerPalette.Surface,
        border = BorderStroke(
            1.dp,
            if (selected) CareerPalette.Moss else CareerPalette.Border,
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = record.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = CareerPalette.Ink,
                )
                Tag(
                    if (record.summaryStatus == "failed") "总结失败" else "已结束",
                    if (record.summaryStatus == "failed") CareerPalette.Error else CareerPalette.Moss,
                )
            }
            Text(
                text = "${record.topic} · ${difficultyLabel(record.difficulty)} · ${record.answers.size} 道回答",
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Muted,
            )
            Text(
                text = record.createdAt,
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Muted,
            )
        }
    }
}

@Composable
private fun RecordDetail(detail: PracticeRecordDetail) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("练习详情", detail.createdAt)
        PrepSectionCard {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Tag(detail.role)
                Tag(detail.topic, CareerPalette.Warning)
                Tag(difficultyLabel(detail.difficulty), CareerPalette.Muted)
                Tag(if (detail.status == "ended") "已结束" else "进行中")
            }
        }
        PrepSectionCard {
            SectionLabel(
                "结束总结",
                trailing = if (detail.summaryStatus == "failed") "生成失败" else "已完成",
            )
            if (detail.summaryStatus == "failed") {
                Text(
                    text = "总结生成失败，已有题目和回答均已保留。桥接后可在这里重试。",
                    color = CareerPalette.Error,
                )
            } else {
                FeedbackList("做得较好", detail.strengths)
                FeedbackList("需要补强", detail.gaps)
                FeedbackList("下一步", detail.nextSteps)
            }
        }
        if (detail.answers.isEmpty()) {
            EmptyPrepState(
                title = "本次没有回答记录",
                description = "记录已结束，但尚未提交可复盘的答案。",
            )
        } else {
            detail.answers.forEachIndexed { index, answer ->
                AnswerCard(index, answer)
            }
        }
    }
}

@Composable
private fun AnswerCard(index: Int, answer: PracticeAnswer) {
    PrepSectionCard {
        Text(
            text = "题目 ${index + 1}",
            style = MaterialTheme.typography.labelLarge,
            color = CareerPalette.Moss,
        )
        Text(
            text = answer.question,
            style = MaterialTheme.typography.titleMedium,
            color = CareerPalette.Ink,
        )
        Text(
            text = "我的回答：${answer.answer}",
            style = MaterialTheme.typography.bodyMedium,
            color = CareerPalette.Muted,
        )
        FeedbackList("已覆盖", answer.coveredPoints)
        FeedbackList("遗漏要点", answer.missingPoints)
        Text(
            text = "追问：${answer.followUp}",
            style = MaterialTheme.typography.bodyMedium,
            color = CareerPalette.Warning,
        )
    }
}

@Composable
private fun FeedbackList(title: String, items: List<String>) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = CareerPalette.Ink,
    )
    items.forEach { item ->
        Text(
            text = "• $item",
            style = MaterialTheme.typography.bodyMedium,
            color = CareerPalette.Muted,
        )
    }
}

@Composable
private fun RecordInput(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
    )
}

private fun difficultyLabel(value: String): String = when (value) {
    "easy" -> "基础"
    "medium" -> "进阶"
    "hard" -> "深入"
    else -> "未指定"
}
