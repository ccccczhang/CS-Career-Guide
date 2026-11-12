package com.cscareerguide.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.data.model.InterviewConfig
import com.cscareerguide.app.data.model.InterviewMessage
import com.cscareerguide.app.data.model.InterviewSpeaker
import com.cscareerguide.app.ui.careerprep.InterviewUiState
import com.cscareerguide.app.ui.theme.CareerPalette
import kotlinx.coroutines.delay

@Composable
fun InterviewScreen(
    state: InterviewUiState,
    config: InterviewConfig,
    onSelectStyle: (String) -> Unit,
    onSelectDuration: (Int) -> Unit,
    onToggleVoiceMock: () -> Unit,
    onStart: () -> Unit,
    onSendAnswer: (String) -> Unit,
    onPause: () -> Unit,
    onFinish: () -> Unit,
    onReset: () -> Unit,
    onTick: () -> Unit,
) {
    var answer by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(state.isRunning, state.isPaused) {
        while (state.isRunning && !state.isPaused) {
            delay(1_000)
            onTick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .testTag("screen_interview"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CareerPrepHeader(
            eyebrow = "AI 模拟面试",
            title = if (state.isRunning) "面试进行中" else "先选好这次练习目标",
            subtitle = "文字优先，语音按钮使用明确标注的本地 Mock 状态。",
        )

        MockStatusBanner("对话、计时和复盘均为本地模拟，不调用模型、ASR 或 TTS。")

        when {
            state.reviewLoading -> {
                PrepSectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text(
                            text = "正在生成本地复盘…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = CareerPalette.Muted,
                        )
                    }
                }
            }

            state.review != null -> {
                InterviewReviewCard(
                    state = state,
                    config = config,
                    onReset = {
                        answer = ""
                        onReset()
                    },
                )
            }

            !state.isRunning -> {
                InterviewSetup(
                    state = state,
                    config = config,
                    onSelectStyle = onSelectStyle,
                    onSelectDuration = onSelectDuration,
                    onToggleVoiceMock = onToggleVoiceMock,
                    onStart = onStart,
                )
            }

            else -> {
                InterviewSession(
                    state = state,
                    answer = answer,
                    onAnswerChange = { answer = it },
                    onSend = {
                        onSendAnswer(answer)
                        answer = ""
                    },
                    onPause = onPause,
                    onFinish = onFinish,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun InterviewSetup(
    state: InterviewUiState,
    config: InterviewConfig,
    onSelectStyle: (String) -> Unit,
    onSelectDuration: (Int) -> Unit,
    onToggleVoiceMock: () -> Unit,
    onStart: () -> Unit,
) {
    PrepSectionCard {
        SectionLabel("选择面试风格", "开始前可以更改")
        config.styles.forEach { style ->
            val selected = style.id == state.styleId
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectStyle(style.id) }
                    .testTag("interview_style_${style.id}"),
                shape = RoundedCornerShape(14.dp),
                color = if (selected) CareerPalette.AccentSoft else CareerPalette.Surface,
                border = BorderStroke(
                    1.dp,
                    if (selected) CareerPalette.Moss else CareerPalette.Border,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape = CircleShape,
                        color = if (selected) CareerPalette.Moss else CareerPalette.Border,
                    ) {}
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = style.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = CareerPalette.Ink,
                        )
                        Text(
                            text = style.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CareerPalette.Muted,
                        )
                    }
                    if (selected) Tag("已选")
                }
            }
        }
    }

    PrepSectionCard {
        SectionLabel("面试时长", "随时可以提前结束")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            config.durationsMinutes.forEach { minutes ->
                val selected = minutes == state.durationMinutes
                Surface(
                    modifier = Modifier
                        .clickable { onSelectDuration(minutes) }
                        .testTag("interview_duration_$minutes"),
                    shape = RoundedCornerShape(50),
                    color = if (selected) CareerPalette.Accent else CareerPalette.AccentSoft,
                ) {
                    Text(
                        text = "$minutes 分钟",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) CareerPalette.Surface else CareerPalette.Moss,
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mock 语音输入",
                    style = MaterialTheme.typography.titleMedium,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = "只切换界面状态，不会请求麦克风权限。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
            Switch(
                checked = state.voiceMockEnabled,
                onCheckedChange = { onToggleVoiceMock() },
                modifier = Modifier.testTag("interview_voice_mock"),
            )
        }
    }

    PrepSectionCard {
        SectionLabel("开始前的小检查")
        Text("• 先保存最新简历，面试内容会更接近你的经历。")
        Text("• 按“背景 → 任务 → 行动 → 结果”组织回答。")
        Text("• 不确定的内容可以说明思路，不要编造经历和数字。")
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("interview_start"),
            colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
        ) {
            Text("开始面试")
        }
    }
}

@Composable
private fun InterviewSession(
    state: InterviewUiState,
    answer: String,
    onAnswerChange: (String) -> Unit,
    onSend: () -> Unit,
    onPause: () -> Unit,
    onFinish: () -> Unit,
) {
    PrepSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "剩余时间",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
                Text(
                    text = formatSeconds(state.remainingSeconds),
                    style = MaterialTheme.typography.headlineMedium,
                    color = CareerPalette.Moss,
                    modifier = Modifier.testTag("interview_timer"),
                )
            }
            if (state.isPaused) Tag("已暂停", CareerPalette.Warning)
            OutlinedButton(onClick = onPause) {
                Icon(Icons.Rounded.Pause, contentDescription = null)
                Text(if (state.isPaused) "继续" else "暂停")
            }
            OutlinedButton(
                onClick = onFinish,
                modifier = Modifier.testTag("interview_finish"),
            ) {
                Text("结束")
            }
        }
        if (state.voiceMockEnabled) {
            MockStatusBanner("Mock 语音状态：已开启，但不会访问真实麦克风。")
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel("面试对话", "Mock 面试官会根据回答生成下一问")
        state.messages.forEach { message ->
            InterviewBubble(message)
        }
        if (state.isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                Text(
                    text = "面试官正在组织问题…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
        }
    }

    PrepSectionCard {
        OutlinedTextField(
            value = answer,
            onValueChange = onAnswerChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("interview_answer"),
            label = { Text("你的回答") },
            placeholder = { Text("说明背景、你的行动和结果…") },
            minLines = 4,
            maxLines = 8,
            shape = RoundedCornerShape(12.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(0.8f),
            ) {
                Icon(Icons.Rounded.Mic, contentDescription = null)
                Text(if (state.voiceMockEnabled) "Mock 聆听" else "语音未开启")
            }
            Button(
                onClick = onSend,
                enabled = answer.isNotBlank() && !state.isLoading && !state.isPaused,
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("interview_send"),
                colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
            ) {
                Text(if (state.isLoading) "等待回复" else "发送回答")
                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null)
            }
        }
    }
}

@Composable
private fun InterviewBubble(message: InterviewMessage) {
    val isUser = message.speaker == InterviewSpeaker.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!isUser) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = CareerPalette.AccentSoft,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "AI",
                        style = MaterialTheme.typography.labelLarge,
                        color = CareerPalette.Moss,
                    )
                }
            }
            Spacer(Modifier.size(8.dp))
        }
        Surface(
            modifier = Modifier.fillMaxWidth(0.86f),
            shape = RoundedCornerShape(
                topStart = if (isUser) 16.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp,
            ),
            color = if (isUser) CareerPalette.Accent else CareerPalette.Surface,
            border = if (isUser) null else BorderStroke(1.dp, CareerPalette.Border),
        ) {
            Column(
                modifier = Modifier.padding(13.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = if (isUser) "我的回答" else "面试官",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isUser) CareerPalette.Surface.copy(alpha = 0.72f) else CareerPalette.Muted,
                )
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) CareerPalette.Surface else CareerPalette.Ink,
                )
            }
        }
    }
}

@Composable
private fun InterviewReviewCard(
    state: InterviewUiState,
    config: InterviewConfig,
    onReset: () -> Unit,
) {
    val review = state.review ?: return
    val styleTitle = config.styles.firstOrNull { it.id == state.styleId }?.title ?: review.style
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PrepSectionCard {
            Text(
                text = "面试复盘 · $styleTitle",
                style = MaterialTheme.typography.titleLarge,
                color = CareerPalette.Ink,
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = review.score.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = CareerPalette.Moss,
                )
                Text(
                    text = "/ 100 · Mock 参考分",
                    modifier = Modifier.padding(bottom = 6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
            Text(
                text = review.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = CareerPalette.Ink,
            )
        }
        ReviewPoints("做得好的地方", review.strengths, CareerPalette.Moss)
        ReviewPoints("下一步重点", review.improvements, CareerPalette.Warning)
        ReviewPoints("建议练习", review.nextActions, CareerPalette.Ink)
        Button(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("interview_again"),
            colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
        ) {
            Text("再练一次")
        }
    }
}

@Composable
private fun ReviewPoints(
    title: String,
    points: List<String>,
    accent: androidx.compose.ui.graphics.Color,
) {
    PrepSectionCard {
        SectionLabel(title)
        points.forEach { point ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text("•", color = accent, fontWeight = FontWeight.Bold)
                Text(
                    text = point,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
        }
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds.coerceAtLeast(0) / 60
    val seconds = totalSeconds.coerceAtLeast(0) % 60
    return "%02d:%02d".format(minutes, seconds)
}
