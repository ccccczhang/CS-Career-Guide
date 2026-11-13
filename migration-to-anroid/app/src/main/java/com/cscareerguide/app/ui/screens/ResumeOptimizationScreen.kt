package com.cscareerguide.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.data.model.ResumeSuggestion
import com.cscareerguide.app.ui.theme.CareerPalette
import com.cscareerguide.app.ui.workspace.ResumeOptimizationUiState

@Composable
fun ResumeOptimizationScreen(
    state: ResumeOptimizationUiState,
    currentRoute: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onUpdateJd: (String) -> Unit,
    onUpdateRevision: (String) -> Unit,
    onAnalyze: () -> Unit,
    onToggleSuggestion: (String) -> Unit,
    onApply: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .testTag("screen_platform_resume"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PlatformWorkspaceHeader(
            title = "简历优化",
            description = "对照目标岗位提出有原文依据的建议，确认前保持当前简历不变。",
            stageLabel = "证据与确认 · 待后端实现",
            currentRoute = currentRoute,
            onBack = onBack,
            onNavigate = onNavigate,
        )
        MockStatusBanner("正式分析与建议应用路由已接入；后端未实现时会明确显示待开发，不生成模拟成功。")

        PrepSectionCard {
            SectionLabel("目标岗位 JD", "建议不超过一个完整职位描述")
            OutlinedTextField(
                value = state.jdText,
                onValueChange = onUpdateJd,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("resume_optimization_jd"),
                label = { Text("粘贴 JD") },
                placeholder = { Text("例如：负责 Java 后端接口设计，要求掌握 Spring Boot、MySQL…") },
                minLines = 6,
                maxLines = 12,
                shape = RoundedCornerShape(12.dp),
            )
            OutlinedTextField(
                value = state.resumeRevision.toString(),
                onValueChange = onUpdateRevision,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("当前简历版本") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
            Button(
                onClick = onAnalyze,
                enabled = !state.busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("resume_optimization_analyze"),
                colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
            ) {
                Text(if (state.busy) "分析中…" else "分析简历")
            }
        }

        PlatformStatus(state.status, isError = state.isError)

        val analysis = state.analysis
        if (analysis != null) {
            SectionLabel(
                title = "原文、证据与修改建议",
                subtitle = "分析记录 ${analysis.id} · 简历版本 ${analysis.resumeRevision}",
            )
            analysis.suggestions.forEach { suggestion ->
                SuggestionCard(
                    suggestion = suggestion,
                    selected = suggestion.id in state.acceptedSuggestionIds,
                    onToggle = { onToggleSuggestion(suggestion.id) },
                )
            }
            OutlinedButton(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("resume_optimization_apply"),
            ) {
                Text("确认应用 ${state.acceptedSuggestionIds.size} 条建议")
            }
        } else {
            EmptyPrepState(
                title = "等待 JD 分析",
                description = "提交后会展示原文、引用证据和建议，不会自动修改简历。",
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun SuggestionCard(
    suggestion: ResumeSuggestion,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("resume_suggestion_${suggestion.id}"),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) CareerPalette.AccentSoft else CareerPalette.Surface,
        border = BorderStroke(
            1.dp,
            if (selected) CareerPalette.Moss else CareerPalette.Border,
        ),
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = suggestion.section,
                        style = MaterialTheme.typography.titleMedium,
                        color = CareerPalette.Ink,
                    )
                    Text(
                        text = if (selected) "已选择，确认后才会应用" else "尚未选择",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CareerPalette.Muted,
                    )
                }
                Checkbox(
                    checked = selected,
                    onCheckedChange = { onToggle() },
                )
            }
            Text(
                text = "原文：${suggestion.original}",
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Muted,
            )
            Text(
                text = "证据：${suggestion.evidence}",
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Warning,
            )
            Text(
                text = "建议：${suggestion.suggestion}",
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Ink,
            )
        }
    }
}
