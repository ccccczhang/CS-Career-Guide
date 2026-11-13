package com.cscareerguide.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cscareerguide.app.data.model.IntroductionDraft
import com.cscareerguide.app.data.model.InterviewReview
import com.cscareerguide.app.data.model.Recommendation
import com.cscareerguide.app.data.model.ResumeDraft
import com.cscareerguide.app.data.model.UserProfile
import com.cscareerguide.app.ui.navigation.Routes
import com.cscareerguide.app.ui.theme.CareerPalette

@Composable
fun ProfileScreen(
    user: UserProfile?,
    introduction: IntroductionDraft,
    recommendations: List<Recommendation>,
    resume: ResumeDraft,
    interviewReview: InterviewReview?,
    recordCount: Int,
    onUpdateProfile: (
        displayName: String,
        school: String,
        major: String,
        grade: String,
        education: String,
        email: String,
        address: String,
        profile: String,
    ) -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    profileSaving: Boolean = false,
    profileError: String? = null,
) {
    var showEdit by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .testTag("screen_profile"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CareerPrepHeader(
            eyebrow = "个人中心",
            title = "把每一次准备，留在这里",
            subtitle = "整理资料、查看简历，回顾职业规划与面试练习。",
            trailing = {
                Surface(
                    modifier = Modifier.clickable {
                        onNavigate(Routes.INTERVIEW)
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = CareerPalette.AccentSoft,
                ) {
                    Text(
                        text = "去面试",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = CareerPalette.Moss,
                    )
                }
            },
        )
        MockStatusBanner("个人资料和简历已接入服务端；推荐和复盘仍为阶段数据。")
        if (profileSaving) {
            Text(
                text = "正在保存个人资料…",
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Moss,
            )
        }
        profileError?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        PrepSectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    modifier = Modifier.size(68.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = CareerPalette.AccentSoft,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (user?.displayName ?: "我").take(1),
                            style = MaterialTheme.typography.headlineMedium,
                            color = CareerPalette.Moss,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = user?.displayName ?: "我的账号",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleLarge,
                            color = CareerPalette.Ink,
                        )
                        Surface(
                            modifier = Modifier
                                .clickable { showEdit = true }
                                .testTag("profile_edit"),
                            shape = RoundedCornerShape(8.dp),
                            color = CareerPalette.AccentSoft,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Rounded.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = CareerPalette.Moss,
                                )
                                Text(
                                    text = "编辑",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CareerPalette.Moss,
                                )
                            }
                        }
                    }
                    Text(
                        text = user?.profile.orEmpty().ifBlank {
                            "写下你的专业方向与兴趣，让个人资料更完整。"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = CareerPalette.Muted,
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Tag(user?.school.orEmpty().ifBlank { "未填写学校" })
                Tag(user?.major.orEmpty().ifBlank { "未填写专业" }, CareerPalette.Warning)
                Tag(user?.grade.orEmpty().ifBlank { "未填写年级" }, CareerPalette.Muted)
                Tag(user?.education.orEmpty().ifBlank { "未填写学历" }, CareerPalette.Muted)
            }
            Text(
                text = listOfNotNull(
                    user?.email?.takeIf(String::isNotBlank),
                    user?.address?.takeIf(String::isNotBlank),
                ).joinToString(" · ").ifBlank { "邮箱和地址待补充" },
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Muted,
            )
        }

        SectionLabel("下一步", "资料、规划、求职与练习入口")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ProfileAction("完善自我介绍") {
                onNavigate(Routes.selfIntroduction("employment"))
            }
            ProfileAction("查看职业规划") {
                onNavigate(Routes.CAREER_EVALUATION)
            }
            ProfileAction("编辑简历") {
                onNavigate(Routes.RESUME)
            }
            ProfileAction("浏览企业与职位") {
                onNavigate(Routes.COMPANY_REVIEWS)
            }
            ProfileAction("练习记录 · $recordCount") {
                onNavigate(Routes.PLATFORM_RECORDS)
            }
        }

        PrepSectionCard {
            SectionLabel("职业推荐", "${recommendations.size} 个方向")
            if (recommendations.isEmpty()) {
                Text(
                    text = "还没有职业推荐，先完善自我介绍并完成规划。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            } else {
                recommendations.take(3).forEachIndexed { index, recommendation ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(34.dp),
                            shape = CircleShape,
                            color = CareerPalette.AccentSoft,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = (index + 1).toString().padStart(2, '0'),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CareerPalette.Moss,
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = recommendation.career,
                                style = MaterialTheme.typography.titleMedium,
                                color = CareerPalette.Ink,
                            )
                            Text(
                                text = "${recommendation.matchScore}% 匹配 · ${recommendation.reason}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CareerPalette.Muted,
                            )
                        }
                    }
                }
            }
        }

        PrepSectionCard {
            SectionLabel(
                title = "我的简历",
                subtitle = resume.targetPosition.ifBlank { "意向岗位待补充" },
            )
            Text(
                text = resume.name.ifBlank { "简历姓名待填写" },
                style = MaterialTheme.typography.titleMedium,
                color = CareerPalette.Ink,
            )
            if (resume.education.isNotEmpty()) {
                Text(
                    text = resume.education.take(2).joinToString("；") {
                        listOf(it.school, it.major, it.degree).filter(String::isNotBlank).joinToString(" · ")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
            Text(
                text = resume.skills.ifBlank { "专业技能待补充" },
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Muted,
                maxLines = 4,
            )
            OutlinedButton(
                onClick = { onNavigate(Routes.RESUME) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("打开简历工作台")
            }
        }

        PrepSectionCard {
            SectionLabel(
                title = "最近面试复盘",
                subtitle = interviewReview?.summary ?: "还没有完成的 Mock 面试复盘",
            )
            if (interviewReview == null) {
                Button(
                    onClick = { onNavigate(Routes.INTERVIEW) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
                ) {
                    Text("开始模拟面试")
                }
            } else {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = interviewReview.score.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = CareerPalette.Moss,
                    )
                    Text(
                        text = "/ 100 · ${interviewReview.style}",
                        modifier = Modifier.padding(bottom = 3.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CareerPalette.Muted,
                    )
                }
                interviewReview.nextActions.take(2).forEach {
                    Text(
                        text = "• $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CareerPalette.Muted,
                    )
                }
            }
        }

        IntroductionPreview(introduction)

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("logout_button"),
        ) {
            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
            Text("退出登录")
        }
        Spacer(Modifier.height(4.dp))
    }

    if (showEdit) {
        ProfileEditDialog(
            user = user,
            onDismiss = { showEdit = false },
            onSave = { values ->
                onUpdateProfile(
                    values[0],
                    values[1],
                    values[2],
                    values[3],
                    values[4],
                    values[5],
                    values[6],
                    values[7],
                )
                showEdit = false
            },
        )
    }
}

@Composable
private fun IntroductionPreview(introduction: IntroductionDraft) {
    PrepSectionCard {
        SectionLabel(
            title = "自我介绍摘要",
            subtitle = if (introduction.completedItems == 0) {
                "尚未填写"
            } else {
                "已完成 ${introduction.progress}%"
            },
        )
        if (introduction.completedItems == 0) {
            Text(
                text = "记录你的经历、技能和职业目标，规划页会据此生成推荐。",
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Muted,
            )
        } else {
            Text(
                text = listOf(
                    introduction.name,
                    introduction.school,
                    introduction.major,
                    introduction.goal,
                ).filter(String::isNotBlank).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Ink,
            )
            if (introduction.skills.isNotEmpty()) {
                Text(
                    text = "技能：${introduction.skills.joinToString("、")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
        }
    }
}

@Composable
private fun ProfileAction(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = CareerPalette.Surface,
        border = BorderStroke(1.dp, CareerPalette.Border),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = CareerPalette.Moss,
        )
    }
}

@Composable
private fun ProfileEditDialog(
    user: UserProfile?,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit,
) {
    var name by remember(user) { mutableStateOf(user?.displayName.orEmpty()) }
    var school by remember(user) { mutableStateOf(user?.school.orEmpty()) }
    var major by remember(user) { mutableStateOf(user?.major.orEmpty()) }
    var grade by remember(user) { mutableStateOf(user?.grade.orEmpty()) }
    var education by remember(user) { mutableStateOf(user?.education.orEmpty()) }
    var email by remember(user) { mutableStateOf(user?.email.orEmpty()) }
    var address by remember(user) { mutableStateOf(user?.address.orEmpty()) }
    var profile by remember(user) { mutableStateOf(user?.profile.orEmpty()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CareerPalette.Surface,
            border = BorderStroke(1.dp, CareerPalette.Border),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "编辑个人资料",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        color = CareerPalette.Ink,
                    )
                    Surface(
                        modifier = Modifier.clickable(onClick = onDismiss),
                        shape = CircleShape,
                        color = CareerPalette.AccentSoft,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "关闭",
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }
                DialogInput("显示名称", name) { name = it }
                DialogInput("学校", school) { school = it }
                DialogInput("专业", major) { major = it }
                DialogInput("年级", grade) { grade = it }
                DialogInput("学历", education) { education = it }
                DialogInput("邮箱", email) { email = it }
                DialogInput("地址", address) { address = it }
                DialogInput("个人简介", profile, minLines = 3) { profile = it }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("取消")
                    }
                    Button(
                        onClick = {
                            onSave(
                                listOf(
                                    name,
                                    school,
                                    major,
                                    grade,
                                    education,
                                    email,
                                    address,
                                    profile,
                                ),
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_save"),
                        colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
                    ) {
                        Text("保存资料")
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogInput(
    label: String,
    value: String,
    minLines: Int = 1,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = minLines == 1,
        minLines = minLines,
        maxLines = if (minLines > 1) 5 else 2,
        shape = RoundedCornerShape(10.dp),
    )
}
