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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.data.model.EvaluationRun
import com.cscareerguide.app.ui.theme.CareerPalette
import com.cscareerguide.app.ui.workspace.EvaluationUiState

@Composable
fun EvaluationScreen(
    state: EvaluationUiState,
    currentRoute: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onUpdateForm: (String?, String?, String?) -> Unit,
    onRun: () -> Unit,
    onOpenRun: (String) -> Unit,
    onExport: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .testTag("screen_platform_evaluation"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PlatformWorkspaceHeader(
            title = "评估与调优",
            description = "管理员在冻结评估集上比较文本策略，并导出可复现的逐例证据。",
            stageLabel = "管理员工具 · Mock",
            currentRoute = currentRoute,
            onBack = onBack,
            onNavigate = onNavigate,
        )
        MockStatusBanner("页面不预填真实指标。当前数字为离线演示，不能用于发布结论。")
        PlatformStatus("管理员权限必须由服务端校验；客户端不根据 profile 字段推断 is_staff。", isError = false)

        PrepSectionCard {
            SectionLabel("评估任务", "题库检索 · 冻结评估集")
            OutlinedTextField(
                value = state.datasetVersion,
                onValueChange = { onUpdateForm(it, null, null) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("评估集版本") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "text-baseline" to "文本基线",
                    "text-balanced" to "均衡权重",
                ).forEach { (value, label) ->
                    Surface(
                        modifier = Modifier
                            .clickable { onUpdateForm(null, value, null) }
                            .testTag("evaluation_strategy_$value"),
                        shape = RoundedCornerShape(50),
                        color = if (state.strategy == value) {
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
                            color = if (state.strategy == value) {
                                CareerPalette.Surface
                            } else {
                                CareerPalette.Muted
                            },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = state.topK,
                onValueChange = { onUpdateForm(null, null, it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Top-K（1–20）") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
            Button(
                onClick = onRun,
                enabled = !state.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("evaluation_run"),
                colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
            ) {
                Text(if (state.loading) "运行中…" else "运行 Mock 评估")
            }
        }

        PlatformStatus(state.status, isError = state.isError)
        state.selected?.let { selected ->
            EvaluationResultCard(selected)
            OutlinedButton(
                onClick = onExport,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("evaluation_export"),
            ) {
                Text("模拟导出 JSON")
            }
        }

        SectionLabel("最近的评估记录", "${state.runs.size} 条")
        if (state.runs.isEmpty()) {
            EmptyPrepState(
                title = "还没有评估记录",
                description = "运行一次评估后，这里按创建时间列出结果。",
            )
        } else {
            state.runs.forEach { run ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenRun(run.id) }
                        .testTag("evaluation_${run.id}"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (state.selected?.id == run.id) {
                        CareerPalette.AccentSoft
                    } else {
                        CareerPalette.Surface
                    },
                    border = BorderStroke(1.dp, CareerPalette.Border),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = run.datasetVersion,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                                color = CareerPalette.Ink,
                            )
                            Tag("K=${run.topK}", CareerPalette.Moss)
                        }
                        Text(
                            text = "${run.strategy} · F1 ${formatMetric(run.metrics.f1AtK)} · MRR ${formatMetric(run.metrics.mrr)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CareerPalette.Muted,
                        )
                        Text(
                            text = "${run.createdAt} · ${run.sampleCount} 条样本",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CareerPalette.Muted,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun EvaluationResultCard(run: EvaluationRun) {
    PrepSectionCard {
        SectionLabel(
            title = "本次结果",
            subtitle = "${run.strategy} · ${run.datasetVersion} · ${run.modelVersion}",
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Metric("Precision@K", run.metrics.precisionAtK)
            Metric("Recall@K", run.metrics.recallAtK)
            Metric("F1@K", run.metrics.f1AtK)
            Metric("HitRate@K", run.metrics.hitRateAtK)
            Metric("MRR", run.metrics.mrr)
        }
        Text(
            text = "样本 ${run.sampleCount} 条 · 失败 ${run.failureCount} 条 · Top-K ${run.topK}",
            style = MaterialTheme.typography.bodyMedium,
            color = CareerPalette.Muted,
        )
    }
}

@Composable
private fun Metric(label: String, value: Double) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CareerPalette.Canvas,
        border = BorderStroke(1.dp, CareerPalette.Border),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Muted,
            )
            Text(
                text = formatMetric(value),
                style = MaterialTheme.typography.titleMedium,
                color = CareerPalette.Ink,
            )
        }
    }
}

private fun formatMetric(value: Double): String = "%.3f".format(value)
