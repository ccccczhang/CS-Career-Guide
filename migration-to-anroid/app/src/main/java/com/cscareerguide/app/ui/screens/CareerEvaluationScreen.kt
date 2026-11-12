package com.cscareerguide.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.data.model.CareerCategory
import com.cscareerguide.app.data.model.Recommendation
import com.cscareerguide.app.ui.planning.PlanningUiState
import com.cscareerguide.app.ui.theme.CareerPalette

@Composable
fun CareerEvaluationScreen(
    state: PlanningUiState,
    onBack: () -> Unit,
    onEditIntroduction: () -> Unit,
    onEnsureRecommendations: () -> Unit,
    onSelectCareer: (String?) -> Unit,
    onCreateReport: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("all") }
    val expanded = remember {
        mutableStateMapOf<String, Boolean>().apply {
            state.careerCategories.forEach { put(it.id, true) }
        }
    }
    val filteredCategories = remember(state.careerCategories, query, category) {
        filterCategories(state.careerCategories, query, category)
    }

    LaunchedEffect(state.hasIntroduction) {
        if (state.hasIntroduction) onEnsureRecommendations()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CareerPalette.Canvas)
            .testTag("screen_career_evaluation"),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "选择职业方向",
                    style = MaterialTheme.typography.titleLarge,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = "先参考推荐，再从全职业图谱中确定一个目标岗位。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("career_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!state.hasIntroduction) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CareerPalette.WarningSoft,
                        border = BorderStroke(1.dp, Color(0xFFEADAB7)),
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "还缺少个人自述",
                                style = MaterialTheme.typography.titleMedium,
                                color = CareerPalette.Warning,
                            )
                            Text(
                                text = "填写姓名、学校、专业、年级、目标职业和至少一项技能后，Mock 推荐才会生成。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CareerPalette.Warning,
                            )
                            OutlinedButton(
                                onClick = onEditIntroduction,
                                modifier = Modifier.testTag("edit_introduction"),
                            ) {
                                Text("填写个人自述")
                            }
                        }
                    }
                }
            } else {
                item {
                    RecommendationSection(state.recommendations, onSelectCareer)
                }
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("career_search"),
                    label = { Text("搜索职业或领域") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true,
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        FilterChip(
                            selected = category == "all",
                            onClick = { category = "all" },
                            label = { Text("全部") },
                        )
                    }
                    items(state.careerCategories) { group ->
                        FilterChip(
                            selected = category == group.id,
                            onClick = { category = group.id },
                            label = { Text(group.title) },
                        )
                    }
                }
            }

            items(filteredCategories) { group ->
                CareerCategoryCard(
                    category = group,
                    selectedCareer = state.selectedCareer,
                    expanded = expanded[group.id] == true,
                    onToggleExpanded = { expanded[group.id] = expanded[group.id] != true },
                    onSelectCareer = onSelectCareer,
                )
            }

            if (filteredCategories.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CareerPalette.Surface,
                        border = BorderStroke(1.dp, CareerPalette.Border),
                    ) {
                        Text(
                            text = "没有匹配的职业，请尝试其他关键词。",
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CareerPalette.Muted,
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CareerPalette.Surface,
            tonalElevation = 3.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "目标岗位",
                        style = MaterialTheme.typography.labelLarge,
                        color = CareerPalette.Muted,
                    )
                    Text(
                        text = state.selectedCareer ?: "尚未选择",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("selected_career"),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (state.selectedCareer == null) CareerPalette.Muted else CareerPalette.Ink,
                    )
                    if (state.selectedCareer != null) {
                        IconButton(onClick = { onSelectCareer(null) }) {
                            Icon(Icons.Rounded.Close, contentDescription = "清除目标岗位")
                        }
                    }
                }
                if (state.introductionError != null) {
                    Text(
                        text = state.introductionError,
                        color = CareerPalette.Error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Button(
                    onClick = onCreateReport,
                    enabled = state.selectedCareer != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("generate_plan"),
                    colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
                ) {
                    Text("生成规划报告")
                    Spacer(Modifier.size(6.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun RecommendationSection(
    recommendations: List<Recommendation>,
    onSelectCareer: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = "为你推荐",
                    style = MaterialTheme.typography.titleLarge,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = "基于当前自述生成的 Mock 结果",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
            Text(
                text = "${recommendations.size} 个方向",
                style = MaterialTheme.typography.labelLarge,
                color = CareerPalette.Moss,
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(recommendations) { recommendation ->
                Surface(
                    modifier = Modifier
                        .width(270.dp)
                        .clickable { onSelectCareer(recommendation.career) }
                        .testTag("recommendation_${recommendation.career}"),
                    shape = RoundedCornerShape(18.dp),
                    color = CareerPalette.Accent,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "MATCH",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFBCC8B5),
                            )
                            Text(
                                text = "${recommendation.matchScore}%",
                                style = MaterialTheme.typography.titleMedium,
                                color = CareerPalette.Surface,
                            )
                        }
                        Text(
                            text = recommendation.career,
                            style = MaterialTheme.typography.titleLarge,
                            color = CareerPalette.Surface,
                        )
                        Text(
                            text = recommendation.reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFDCE5D6),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CareerCategoryCard(
    category: CareerCategory,
    selectedCareer: String?,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectCareer: (String?) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        color = CareerPalette.Surface,
        border = BorderStroke(1.dp, CareerPalette.Border),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = category.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = "${category.roles.size} 项",
                    style = MaterialTheme.typography.labelLarge,
                    color = CareerPalette.Moss,
                )
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "收起" else "展开",
                    tint = CareerPalette.Muted,
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    category.roles.chunked(2).forEach { rowRoles ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowRoles.forEach { role ->
                                val selected = role == selectedCareer
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSelectCareer(role) }
                                        .testTag("career_option_${role}"),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) CareerPalette.AccentSoft else Color(0xFFF8F8F4),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) CareerPalette.Moss else CareerPalette.Border,
                                    ),
                                ) {
                                    Text(
                                        text = role,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selected) CareerPalette.Moss else CareerPalette.Ink,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    )
                                }
                            }
                            if (rowRoles.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

private fun filterCategories(
    categories: List<CareerCategory>,
    query: String,
    categoryId: String,
): List<CareerCategory> {
    val search = query.trim().lowercase()
    return categories
        .filter { categoryId == "all" || it.id == categoryId }
        .map { category ->
            if (search.isBlank() || category.title.lowercase().contains(search)) {
                category
            } else {
                category.copy(
                    roles = category.roles.filter { it.lowercase().contains(search) },
                )
            }
        }
        .filter { it.roles.isNotEmpty() }
}
