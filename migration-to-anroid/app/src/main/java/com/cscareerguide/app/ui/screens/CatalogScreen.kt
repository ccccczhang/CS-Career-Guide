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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.data.model.CatalogItem
import com.cscareerguide.app.data.model.CatalogKind
import com.cscareerguide.app.ui.theme.CareerPalette
import com.cscareerguide.app.ui.workspace.CatalogUiState

@Composable
fun CatalogScreen(
    state: CatalogUiState,
    currentRoute: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onSelectKind: (CatalogKind) -> Unit,
    onUpdateFilters: (String?, String?, String?, String?, String?) -> Unit,
    onSearch: () -> Unit,
    onReset: () -> Unit,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .testTag("screen_platform_catalog"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PlatformWorkspaceHeader(
            title = "岗位、企业与面试题库",
            description = "查询带来源的岗位、企业和题目；公开结果只展示许可通过的记录。",
            stageLabel = "正式目录 · 服务端",
            currentRoute = currentRoute,
            onBack = onBack,
            onNavigate = onNavigate,
        )
        MockStatusBanner("目录、来源和权限状态来自正式接口；空库会显示真实空结果。")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CatalogKind.entries.forEach { kind ->
                val selected = state.kind == kind
                Surface(
                    modifier = Modifier
                        .clickable { onSelectKind(kind) }
                        .testTag("catalog_kind_${kind.name.lowercase()}"),
                    shape = RoundedCornerShape(50),
                    color = if (selected) CareerPalette.Accent else CareerPalette.Surface,
                    border = BorderStroke(
                        1.dp,
                        if (selected) CareerPalette.Accent else CareerPalette.Border,
                    ),
                ) {
                    Text(
                        text = kind.label,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp),
                        color = if (selected) CareerPalette.Surface else CareerPalette.Muted,
                    )
                }
            }
        }

        PrepSectionCard {
            SectionLabel("查询条件", "至少使用一个关键词或字段筛选")
            PlatformInput("关键词", state.query) {
                onUpdateFilters(it, null, null, null, null)
            }
            if (state.kind != CatalogKind.QUESTIONS) {
                PlatformInput("城市", state.city) {
                    onUpdateFilters(null, it, null, null, null)
                }
            }
            PlatformInput(
                if (state.kind == CatalogKind.QUESTIONS) "岗位方向" else "岗位方向（可选）",
                state.role,
            ) {
                onUpdateFilters(null, null, it, null, null)
            }
            if (state.kind == CatalogKind.QUESTIONS) {
                PlatformInput("知识点", state.topic) {
                    onUpdateFilters(null, null, null, it, null)
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("" to "全部难度", "easy" to "基础", "medium" to "进阶", "hard" to "深入")
                        .forEach { (value, label) ->
                            Surface(
                                modifier = Modifier.clickable {
                                    onUpdateFilters(null, null, null, null, value)
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
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                                    color = if (state.difficulty == value) {
                                        CareerPalette.Moss
                                    } else {
                                        CareerPalette.Muted
                                    },
                                )
                            }
                        }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier
                        .weight(0.8f)
                        .testTag("catalog_reset"),
                ) {
                    Text("重置")
                }
                Button(
                    onClick = onSearch,
                    enabled = !state.loading,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("catalog_search"),
                    colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
                ) {
                    Text(if (state.loading) "查询中…" else "查询资料")
                }
            }
        }

        if (state.error != null) {
            PlatformStatus(state.error, isError = true)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(
                title = "${state.kind.label}列表",
                subtitle = "来源可追溯，pending / restricted 不进入公开结果",
            )
        }
        Text(
            text = "${state.total} 条记录" +
                if (state.latestSource.isBlank()) "" else " · 最近来源：${state.latestSource}",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = CareerPalette.Muted,
        )

        if (state.loading && state.items.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.height(22.dp))
                Text("正在读取服务端目录…", color = CareerPalette.Muted)
            }
        } else if (state.items.isEmpty()) {
            EmptyPrepState(
                title = "没有符合条件的记录",
                description = "调整关键词或筛选条件；未审核和受限数据不会补入结果。",
            )
        } else {
            state.items.forEach { item -> CatalogItemCard(item) }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onPreviousPage,
                enabled = state.hasPrevious && !state.loading,
                modifier = Modifier.weight(1f),
            ) {
                Text("上一页")
            }
            Text(
                text = "第 ${state.page} 页",
                modifier = Modifier.padding(vertical = 12.dp),
                color = CareerPalette.Muted,
            )
            OutlinedButton(
                onClick = onNextPage,
                enabled = state.hasNext && !state.loading,
                modifier = Modifier.weight(1f),
            ) {
                Text("下一页")
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun CatalogItemCard(item: CatalogItem) {
    PrepSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = item.subtitle,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
            Tag(if (item.isDemo) "演示" else "正式", CareerPalette.Warning)
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            if (item.city.isNotBlank()) Tag(item.city)
            if (item.role.isNotBlank()) Tag(item.role, CareerPalette.Warning)
            if (item.topic.isNotBlank()) Tag(item.topic, CareerPalette.Muted)
            item.tags.take(3).forEach { Tag(it, CareerPalette.Moss) }
        }
        if (item.description.isNotBlank()) {
            Text(
                text = item.description,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Ink,
            )
        }
        if (item.keyPoints.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "参考要点",
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                    color = CareerPalette.Moss,
                )
                item.keyPoints.forEach { point ->
                    Text(
                        text = "• $point",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = CareerPalette.Muted,
                    )
                }
            }
        }
        Text(
            text = "来源：${item.source} · ${item.collectedAt} · ${item.permissionStatus}",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = CareerPalette.Muted,
        )
    }
}

@Composable
private fun PlatformInput(
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
