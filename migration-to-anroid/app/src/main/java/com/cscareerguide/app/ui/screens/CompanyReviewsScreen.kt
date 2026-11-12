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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.data.model.CompanyRanking
import com.cscareerguide.app.data.model.JobSummary
import com.cscareerguide.app.data.model.SeniorAdvice
import com.cscareerguide.app.data.model.SeniorAdviceDraft
import com.cscareerguide.app.ui.careerprep.AdviceFormUiState
import com.cscareerguide.app.ui.careerprep.HuntingTab
import com.cscareerguide.app.ui.careerprep.HuntingUiState
import com.cscareerguide.app.ui.theme.CareerPalette

@Composable
fun CompanyReviewsScreen(
    state: HuntingUiState,
    onSelectTab: (HuntingTab) -> Unit,
    onSearch: (String) -> Unit,
    onShareAdvice: () -> Unit,
    onOpenResume: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .testTag("screen_company_reviews"),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        CareerPrepHeader(
            eyebrow = "求职准备",
            title = "企业、岗位与经验",
            subtitle = "先用现有信息建立判断，再到原招聘页面核实。",
            trailing = {
                Surface(
                    modifier = Modifier
                        .clickable(onClick = onOpenResume)
                        .testTag("company_resume"),
                    shape = RoundedCornerShape(10.dp),
                    color = CareerPalette.AccentSoft,
                ) {
                    Text(
                        text = "简历",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = CareerPalette.Moss,
                    )
                }
            },
        )

        MockStatusBanner("岗位、榜单与前辈经验均为离线示例，不代表真实招聘结果。")

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(HuntingTab.entries) { tab ->
                val selected = tab == state.selectedTab
                Surface(
                    modifier = Modifier
                        .clickable { onSelectTab(tab) }
                        .testTag("hunting_tab_${tab.name.lowercase()}"),
                    shape = RoundedCornerShape(50),
                    color = if (selected) CareerPalette.Accent else CareerPalette.Surface,
                    border = BorderStroke(1.dp, if (selected) CareerPalette.Accent else CareerPalette.Border),
                ) {
                    Text(
                        text = tab.label,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) CareerPalette.Surface else CareerPalette.Muted,
                    )
                }
            }
        }

        when (state.selectedTab) {
            HuntingTab.JOBS -> {
                SearchField(
                    value = state.search,
                    placeholder = "搜索职位、公司、地区或行业",
                    onValueChange = onSearch,
                )
                Text(
                    text = "全部职位 · ${state.visibleJobs.size} 条",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
                if (state.visibleJobs.isEmpty()) {
                    EmptyPrepState(
                        title = "没有找到匹配职位",
                        description = "尝试公司、职位或地区关键词，或清空搜索查看全部内容。",
                    )
                } else {
                    state.visibleJobs.forEach { JobCard(it) }
                }
            }

            HuntingTab.RED -> RankingList(
                title = "红榜 · 值得关注",
                description = "作为了解岗位的参考，具体招聘信息仍需核实。",
                items = state.positiveRankings,
            )

            HuntingTab.BLACK -> RankingList(
                title = "黑榜 · 提前了解",
                description = "榜单记录的是待核实的风险线索，不代表最终结论。",
                items = state.negativeRankings,
            )

            HuntingTab.ADVICE -> {
                SearchField(
                    value = state.search,
                    placeholder = "搜索公司、岗位或经验内容",
                    onValueChange = onSearch,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "来自前辈的经验 · ${state.visibleAdvice.size} 条",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CareerPalette.Muted,
                    )
                    Surface(
                        modifier = Modifier
                            .clickable(onClick = onShareAdvice)
                            .testTag("share_advice"),
                        shape = RoundedCornerShape(10.dp),
                        color = CareerPalette.AccentSoft,
                    ) {
                        Text(
                            text = "分享经验",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = CareerPalette.Moss,
                        )
                    }
                }
                if (state.visibleAdvice.isEmpty()) {
                    EmptyPrepState(
                        title = "没有找到匹配经验",
                        description = "清空搜索，或分享一条新的岗位体验。",
                    )
                } else {
                    state.visibleAdvice.forEach { AdviceCard(it) }
                }
            }

            HuntingTab.SAFETY -> SafetyContent()
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
fun SeniorAdviceScreen(
    state: AdviceFormUiState,
    onUpdate: ((SeniorAdviceDraft) -> SeniorAdviceDraft) -> Unit,
    onSubmit: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .testTag("screen_senior_advice_form"),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        CareerPrepHeader(
            eyebrow = "分享经验",
            title = "把你的经历留给下一位",
            subtitle = "提交后写入本地 Mock 列表，不会发布到真实服务。",
            onBack = onBack,
        )

        if (state.successMessage != null) {
            PrepSectionCard {
                Text(
                    text = "分享已保存",
                    style = MaterialTheme.typography.titleLarge,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = state.successMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
                Text(
                    text = "返回企业页的前辈经验栏目，可以直接看到刚刚新增的内容。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Moss,
                )
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advice_success_back"),
                    colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
                ) {
                    Text("返回前辈经验")
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onReset),
                    shape = RoundedCornerShape(10.dp),
                    color = CareerPalette.AccentSoft,
                ) {
                    Text(
                        text = "继续分享一条",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = CareerPalette.Moss,
                    )
                }
            }
            return@Column
        }

        if (state.error != null) {
            MockStatusBanner(state.error, isError = true)
        }

        PrepSectionCard {
            SectionLabel("基本信息", "带 * 的项目为必填")
            FormField(
                label = "公司名称 *",
                value = state.draft.company,
                onValueChange = { value -> onUpdate { it.copy(company = value) } },
            )
            FormField(
                label = "岗位名称 *",
                value = state.draft.position,
                onValueChange = { value -> onUpdate { it.copy(position = value) } },
            )
            FormField(
                label = "分享人称呼 *",
                value = state.draft.author,
                onValueChange = { value -> onUpdate { it.copy(author = value) } },
            )
            FormField(
                label = "毕业年份 *",
                value = state.draft.graduationYear,
                onValueChange = { value ->
                    onUpdate { it.copy(graduationYear = value.filter(Char::isDigit).take(4)) }
                },
            )
            FormField(
                label = "当前公司（选填）",
                value = state.draft.currentCompany,
                onValueChange = { value -> onUpdate { it.copy(currentCompany = value) } },
            )
        }

        PrepSectionCard {
            SectionLabel("工作体验", "具体、可核实的描述更有帮助")
            FormField(
                label = "详细建议 *",
                value = state.draft.advice,
                onValueChange = { value -> onUpdate { it.copy(advice = value) } },
                minLines = 5,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormField(
                    label = "评分 *（1–5）",
                    value = state.draft.rating,
                    onValueChange = { value ->
                        onUpdate { it.copy(rating = value.filter { char -> char.isDigit() || char == '.' }) }
                    },
                    modifier = Modifier.weight(0.7f),
                )
                FormField(
                    label = "薪资参考（选填）",
                    value = state.draft.salaryInfo,
                    onValueChange = { value -> onUpdate { it.copy(salaryInfo = value) } },
                    modifier = Modifier.weight(1.3f),
                )
            }
            FormField(
                label = "值得关注（选填，用逗号分隔）",
                value = state.draft.pros,
                onValueChange = { value -> onUpdate { it.copy(pros = value) } },
            )
            FormField(
                label = "需要了解（选填，用逗号分隔）",
                value = state.draft.cons,
                onValueChange = { value -> onUpdate { it.copy(cons = value) } },
            )
        }

        Button(
            onClick = onSubmit,
            enabled = !state.submitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("advice_submit"),
            colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
        ) {
            Text(if (state.submitting) "正在保存…" else "提交本地 Mock 经验")
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun SearchField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hunting_search"),
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
private fun JobCard(job: JobSummary) {
    PrepSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = job.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = job.company,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
            Text(
                text = job.salary,
                style = MaterialTheme.typography.labelLarge,
                color = CareerPalette.Moss,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Tag(job.city)
            if (job.industry.isNotBlank()) Tag(job.industry, CareerPalette.Warning)
            if (job.companySize.isNotBlank()) Tag(job.companySize, CareerPalette.Muted)
        }
        Text(
            text = job.description,
            style = MaterialTheme.typography.bodyMedium,
            color = CareerPalette.Ink,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = job.companyDescription,
            style = MaterialTheme.typography.bodyMedium,
            color = CareerPalette.Muted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "申请入口未接入",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Muted,
            )
            Tag("仅作 UI 演示", CareerPalette.Warning)
        }
    }
}

@Composable
private fun RankingList(
    title: String,
    description: String,
    items: List<CompanyRanking>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel(title, description)
        items.forEach { item ->
            PrepSectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (item.isPositive) CareerPalette.AccentSoft else CareerPalette.ErrorSoft,
                    ) {
                        Text(
                            text = item.rank.toString().padStart(2, '0'),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (item.isPositive) CareerPalette.Moss else CareerPalette.Error,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = CareerPalette.Ink,
                        )
                        Text(
                            text = item.company,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CareerPalette.Muted,
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Tag(item.city)
                    if (item.salary.isNotBlank()) Tag(item.salary, CareerPalette.Warning)
                }
                Text(
                    text = item.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Ink,
                )
            }
        }
    }
}

@Composable
private fun AdviceCard(advice: SeniorAdvice) {
    PrepSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = advice.company,
                    style = MaterialTheme.typography.titleMedium,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = advice.position,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
            Tag("★ ${advice.rating} / 5", CareerPalette.Warning)
        }
        Text(
            text = "${advice.author} · ${advice.graduationYear} 届",
            style = MaterialTheme.typography.bodyMedium,
            color = CareerPalette.Muted,
        )
        Text(
            text = advice.content,
            style = MaterialTheme.typography.bodyLarge,
            color = CareerPalette.Ink,
        )
        if (advice.currentCompany.isNotBlank() || advice.salaryInfo.isNotBlank()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                if (advice.currentCompany.isNotBlank()) Tag("现就职：${advice.currentCompany}")
                if (advice.salaryInfo.isNotBlank()) Tag("薪资参考：${advice.salaryInfo}", CareerPalette.Warning)
            }
        }
        if (advice.pros.isNotEmpty()) {
            Text(
                text = "值得关注：${advice.pros.joinToString("；")}",
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Moss,
                fontWeight = FontWeight.Medium,
            )
        }
        if (advice.cons.isNotEmpty()) {
            Text(
                text = "需要了解：${advice.cons.joinToString("；")}",
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Warning,
            )
        }
    }
}

@Composable
private fun SafetyContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel(
            title = "求职安全指南",
            subtitle = "先核实，再决定。遇到异常承诺和收费时给自己留出确认时间。",
        )
        listOf(
            "招聘转培训与收费" to listOf(
                "就业岗位不应以先交培训费、押金或办理贷款为前提。",
                "核对招聘主体、岗位内容和付款对象，保留沟通记录。",
            ),
            "虚假招聘与付费内推" to listOf(
                "通过企业官网和正规招聘渠道确认岗位是否真实存在。",
                "警惕低门槛高薪、付费保录和催转账等承诺。",
            ),
            "刷单兼职与信息盗用" to listOf(
                "不参与垫资刷单，不下载来源不明的求职应用。",
                "谨慎提供身份证、银行卡和验证码，不出借个人账号。",
            ),
            "合同与工作安全" to listOf(
                "入职前核对工作内容、薪资、地点和合同约定。",
                "保存招聘信息、聊天记录和合同；遇到侵害及时求助。",
            ),
        ).forEachIndexed { index, (title, points) ->
            PrepSectionCard {
                Text(
                    text = "${(index + 1).toString().padStart(2, '0')} / $title",
                    style = MaterialTheme.typography.titleMedium,
                    color = CareerPalette.Ink,
                )
                points.forEach { point ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text("•", color = CareerPalette.Moss)
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
        PrepSectionCard {
            SectionLabel("遇到疑问时，先做这三件事")
            Text("1. 核实信息：通过官网、学校就业部门或正规渠道交叉确认。")
            Text("2. 保留材料：保存招聘页面、沟通记录、合同和付款凭据。")
            Text("3. 及时求助：咨询学校就业部门、当地人社部门；疑似被骗及时报警。")
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        minLines = minLines,
        maxLines = if (minLines > 1) 8 else 2,
        singleLine = minLines == 1,
        shape = RoundedCornerShape(12.dp),
    )
}
