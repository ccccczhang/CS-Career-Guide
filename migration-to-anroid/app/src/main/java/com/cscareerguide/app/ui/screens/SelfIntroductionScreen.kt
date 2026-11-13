package com.cscareerguide.app.ui.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.data.model.IntroductionDraft
import com.cscareerguide.app.data.model.SkillGroup
import com.cscareerguide.app.ui.planning.DraftSaveStatus
import com.cscareerguide.app.ui.planning.PlanningUiState
import com.cscareerguide.app.ui.theme.CareerPalette

@Composable
fun SelfIntroductionScreen(
    state: PlanningUiState,
    onBack: () -> Unit,
    onUpdate: ((IntroductionDraft) -> IntroductionDraft) -> Unit,
    onToggleSkill: (String) -> Unit,
    onSaveDraft: () -> Unit,
    onNext: () -> Unit,
) {
    val expandedGroups = remember {
        mutableStateMapOf<String, Boolean>().apply {
            state.skillGroups.forEach { put(it.id, true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CareerPalette.Canvas)
            .imePadding()
            .testTag("screen_self_introduction"),
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
                    text = "个人自述",
                    style = MaterialTheme.typography.titleLarge,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = "逐步补充信息，内容会保存到当前 Mock 会话。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            IntroductionProgress(
                progress = state.introduction.progress,
                completed = state.introduction.completedItems,
            )

            IntroductionSection(
                number = "01",
                title = "基本信息与职业目标",
                description = "从学习背景开始，告诉我们你想前往的方向。",
            ) {
                DraftTextField(
                    label = "姓名",
                    value = state.introduction.name,
                    tag = "intro_name",
                    onValueChange = { value -> onUpdate { it.copy(name = value) } },
                )
                DraftTextField(
                    label = "就读学校",
                    value = state.introduction.school,
                    tag = "intro_school",
                    onValueChange = { value -> onUpdate { it.copy(school = value) } },
                )
                DraftTextField(
                    label = "专业",
                    value = state.introduction.major,
                    tag = "intro_major",
                    onValueChange = { value -> onUpdate { it.copy(major = value) } },
                )
                DraftTextField(
                    label = "年级",
                    value = state.introduction.grade,
                    tag = "intro_grade",
                    onValueChange = { value -> onUpdate { it.copy(grade = value) } },
                )
                DraftTextField(
                    label = "目标职业",
                    value = state.introduction.goal,
                    tag = "intro_goal",
                    onValueChange = { value -> onUpdate { it.copy(goal = value) } },
                )
            }

            IntroductionSection(
                number = "02",
                title = "技能模块评估",
                description = "选择已经接触或掌握的技能，可多选。",
            ) {
                state.skillGroups.forEach { group ->
                    SkillGroupCard(
                        group = group,
                        selected = state.introduction.skills,
                        expanded = expandedGroups[group.id] == true,
                        onToggleExpanded = {
                            expandedGroups[group.id] = expandedGroups[group.id] != true
                        },
                        onToggleSkill = onToggleSkill,
                    )
                }
            }

            IntroductionSection(
                number = "03",
                title = "经历与个人偏好",
                description = "具体经历会让后续推荐和规划更有参考价值。",
            ) {
                DraftTextField(
                    label = "其他技能或项目经历",
                    value = state.introduction.otherSkills,
                    tag = "intro_other_skills",
                    minLines = 4,
                    onValueChange = { value -> onUpdate { it.copy(otherSkills = value) } },
                )
                DraftTextField(
                    label = "个人特点与职业偏好",
                    value = state.introduction.selfDescription,
                    tag = "intro_preferences",
                    minLines = 4,
                    onValueChange = { value -> onUpdate { it.copy(selfDescription = value) } },
                )
            }

            if (state.introductionError != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CareerPalette.ErrorSoft,
                    border = BorderStroke(1.dp, Color(0xFFEDC7C1)),
                ) {
                    Text(
                        text = state.introductionError,
                        modifier = Modifier
                            .padding(14.dp)
                            .testTag("intro_error"),
                        color = CareerPalette.Error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CareerPalette.WarningSoft,
            ) {
                Text(
                    text = "Mock 说明：当前内容只保存在应用内存中，不会上传到服务器。",
                    modifier = Modifier.padding(14.dp),
                    color = CareerPalette.Warning,
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
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = draftStatusText(state.draftStatus),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("intro_draft_status"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CareerPalette.Muted,
                    )
                    Text(
                        text = "已填写 ${state.introduction.completedItems} / 8 项",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CareerPalette.Muted,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onSaveDraft,
                        modifier = Modifier
                            .weight(0.38f)
                            .height(50.dp),
                    ) {
                        Text("保存草稿")
                    }
                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .weight(0.62f)
                            .height(50.dp)
                            .testTag("intro_next"),
                        colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
                    ) {
                        Text("下一步：选择方向")
                        Spacer(Modifier.size(6.dp))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroductionProgress(
    progress: Int,
    completed: Int,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = "填写进度",
                    style = MaterialTheme.typography.titleMedium,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = "$progress%",
                    style = MaterialTheme.typography.titleLarge,
                    color = CareerPalette.Moss,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(CareerPalette.AccentSoft, RoundedCornerShape(50)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress / 100f)
                        .height(8.dp)
                        .background(CareerPalette.Moss, RoundedCornerShape(50)),
                )
            }
            Text(
                text = "已完成 $completed / 8 项。进度仅作提示，不阻止继续填写。",
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Muted,
            )
        }
    }
}

@Composable
private fun IntroductionSection(
    number: String,
    title: String,
    description: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CareerPalette.Surface,
        border = BorderStroke(1.dp, CareerPalette.Border),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelLarge,
                    color = CareerPalette.Moss,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = CareerPalette.Ink,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CareerPalette.Muted,
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun DraftTextField(
    label: String,
    value: String,
    tag: String,
    minLines: Int = 1,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        label = { Text(label) },
        minLines = minLines,
        textStyle = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun SkillGroupCard(
    group: SkillGroup,
    selected: Set<String>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onToggleSkill: (String) -> Unit,
) {
    val count = group.skills.count { it.value in selected }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFCFDF9),
        border = BorderStroke(1.dp, CareerPalette.Border),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = group.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = CareerPalette.Ink,
                    )
                    Text(
                        text = group.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CareerPalette.Muted,
                    )
                }
                Text(
                    text = "$count/${group.skills.size}",
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
                HorizontalDivider(color = CareerPalette.Border)
                group.skills.forEachIndexed { index, skill ->
                    val checked = skill.value in selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = checked,
                                role = Role.Checkbox,
                                onValueChange = { onToggleSkill(skill.value) },
                            )
                            .background(if (checked) CareerPalette.AccentSoft else Color.Transparent)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("skill_${group.id}_$index"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = null,
                        )
                        Text(
                            text = skill.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CareerPalette.Ink,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun draftStatusText(status: DraftSaveStatus): String = when (status) {
    DraftSaveStatus.IDLE -> "填写后自动保存"
    DraftSaveStatus.PENDING -> "草稿保存中…"
    DraftSaveStatus.SAVED -> "草稿已保存到 Mock 会话"
    DraftSaveStatus.UNAVAILABLE -> "草稿保存失败"
}
