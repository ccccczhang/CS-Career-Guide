package com.cscareerguide.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.data.model.ResumeCertificate
import com.cscareerguide.app.data.model.ResumeDraft
import com.cscareerguide.app.data.model.ResumeEducation
import com.cscareerguide.app.data.model.ResumeExperience
import com.cscareerguide.app.data.model.ResumeProject
import com.cscareerguide.app.ui.careerprep.ResumePane
import com.cscareerguide.app.ui.careerprep.ResumeSection
import com.cscareerguide.app.ui.careerprep.ResumeUiState
import com.cscareerguide.app.ui.theme.CareerPalette

@Composable
fun ResumeScreen(
    state: ResumeUiState,
    onBack: () -> Unit,
    onSelectPane: (ResumePane) -> Unit,
    onSelectSection: (ResumeSection) -> Unit,
    onUpdateBasic: (String?, String?, String?, String?, String?) -> Unit,
    onUpdateEvaluation: (String) -> Unit,
    onUpdateSkills: (String) -> Unit,
    onAddEducation: () -> Unit,
    onUpdateEducation: (Int, ResumeEducation) -> Unit,
    onDeleteEducation: (Int) -> Unit,
    onAddExperience: () -> Unit,
    onUpdateExperience: (Int, ResumeExperience) -> Unit,
    onDeleteExperience: (Int) -> Unit,
    onAddProject: () -> Unit,
    onUpdateProject: (Int, ResumeProject) -> Unit,
    onDeleteProject: (Int) -> Unit,
    onAddCertificate: () -> Unit,
    onUpdateCertificate: (Int, ResumeCertificate) -> Unit,
    onDeleteCertificate: (Int) -> Unit,
    onSave: () -> Unit,
    onExport: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .testTag("screen_resume"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("route_back")) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "求职准备 / 我的简历",
                    style = MaterialTheme.typography.labelLarge,
                    color = CareerPalette.Moss,
                )
                Text(
                    text = "简历工作台",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = "边编辑、边预览，把经历整理成清晰表达。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onSave,
                enabled = !state.saving,
                modifier = Modifier
                    .weight(1f)
                    .testTag("resume_save"),
            ) {
                Text(if (state.saving) "保存中…" else "保存简历")
            }
            Button(
                onClick = onExport,
                modifier = Modifier
                    .weight(1f)
                    .testTag("resume_export"),
                colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
            ) {
                Text("导出 PDF")
            }
        }

        if (state.notice != null) {
            MockStatusBanner(state.notice, isError = state.noticeIsError)
        }

        ResumePaneSwitch(
            pane = state.pane,
            onSelectPane = onSelectPane,
        )

        if (state.pane == ResumePane.PREVIEW) {
            ResumePreview(draft = state.draft)
        } else {
            ResumeSectionTabs(
                selected = state.section,
                onSelect = onSelectSection,
            )
            when (state.section) {
                ResumeSection.BASIC -> BasicResumeSection(
                    draft = state.draft,
                    onUpdate = onUpdateBasic,
                )

                ResumeSection.EVALUATION -> EvaluationResumeSection(
                    value = state.draft.selfEvaluation,
                    onUpdate = onUpdateEvaluation,
                )

                ResumeSection.EDUCATION -> EducationResumeSection(
                    draft = state.draft,
                    onAdd = onAddEducation,
                    onUpdate = onUpdateEducation,
                    onDelete = onDeleteEducation,
                )

                ResumeSection.EXPERIENCE -> ExperienceResumeSection(
                    draft = state.draft,
                    onAdd = onAddExperience,
                    onUpdate = onUpdateExperience,
                    onDelete = onDeleteExperience,
                )

                ResumeSection.PROJECTS -> ProjectResumeSection(
                    draft = state.draft,
                    onAdd = onAddProject,
                    onUpdate = onUpdateProject,
                    onDelete = onDeleteProject,
                )

                ResumeSection.SKILLS -> SkillsResumeSection(
                    value = state.draft.skills,
                    onUpdate = onUpdateSkills,
                )

                ResumeSection.CERTIFICATES -> CertificateResumeSection(
                    draft = state.draft,
                    onAdd = onAddCertificate,
                    onUpdate = onUpdateCertificate,
                    onDelete = onDeleteCertificate,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun ResumePaneSwitch(
    pane: ResumePane,
    onSelectPane: (ResumePane) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CareerPalette.AccentSoft,
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            ResumePane.entries.forEach { item ->
                val selected = pane == item
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectPane(item) }
                        .testTag("resume_pane_${item.name.lowercase()}"),
                    shape = RoundedCornerShape(9.dp),
                    color = if (selected) CareerPalette.Surface else CareerPalette.AccentSoft,
                ) {
                    Text(
                        text = if (item == ResumePane.EDIT) "编辑" else "实时预览",
                        modifier = Modifier.padding(vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) CareerPalette.Ink else CareerPalette.Muted,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumeSectionTabs(
    selected: ResumeSection,
    onSelect: (ResumeSection) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ResumeSection.entries) { section ->
            val active = section == selected
            Surface(
                modifier = Modifier
                    .clickable { onSelect(section) }
                    .testTag("resume_section_${section.name.lowercase()}"),
                shape = RoundedCornerShape(50),
                color = if (active) CareerPalette.Accent else CareerPalette.Surface,
                border = BorderStroke(
                    1.dp,
                    if (active) CareerPalette.Accent else CareerPalette.Border,
                ),
            ) {
                Text(
                    text = section.label,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) CareerPalette.Surface else CareerPalette.Muted,
                )
            }
        }
    }
}

@Composable
private fun BasicResumeSection(
    draft: ResumeDraft,
    onUpdate: (String?, String?, String?, String?, String?) -> Unit,
) {
    PrepSectionCard {
        SectionLabel("基本信息", "先让招聘方快速知道你是谁、想做什么")
        ResumeInput("姓名", draft.name) { onUpdate(it, null, null, null, null) }
        ResumeInput("意向岗位", draft.targetPosition) { onUpdate(null, it, null, null, null) }
        ResumeInput("电话", draft.phone) { onUpdate(null, null, it, null, null) }
        ResumeInput("邮箱", draft.email) { onUpdate(null, null, null, it, null) }
        ResumeInput("所在城市", draft.address) { onUpdate(null, null, null, null, it) }
    }
}

@Composable
private fun EvaluationResumeSection(
    value: String,
    onUpdate: (String) -> Unit,
) {
    PrepSectionCard {
        SectionLabel("个人自评", "用 2–4 句说明背景、能力与求职方向")
        ResumeInput("个人自评", value, minLines = 5, onValueChange = onUpdate)
    }
}

@Composable
private fun EducationResumeSection(
    draft: ResumeDraft,
    onAdd: () -> Unit,
    onUpdate: (Int, ResumeEducation) -> Unit,
    onDelete: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("教育背景", "最多保留与目标岗位最相关的经历")
        draft.education.forEachIndexed { index, item ->
            PrepSectionCard {
                EntryHeader("教育经历 ${index + 1}", onDelete = { onDelete(index) })
                ResumeInput("学校", item.school) { onUpdate(index, item.copy(school = it)) }
                ResumeInput("专业", item.major) { onUpdate(index, item.copy(major = it)) }
                ResumeInput("学历", item.degree) { onUpdate(index, item.copy(degree = it)) }
                ResumeInput("时间", item.period) { onUpdate(index, item.copy(period = it)) }
                ResumeInput("描述", item.description, minLines = 3) {
                    onUpdate(index, item.copy(description = it))
                }
            }
        }
        AddEntryButton("添加教育经历", onAdd)
    }
}

@Composable
private fun ExperienceResumeSection(
    draft: ResumeDraft,
    onAdd: () -> Unit,
    onUpdate: (Int, ResumeExperience) -> Unit,
    onDelete: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("实习经历", "写清职责、行动和你带来的变化")
        draft.experience.forEachIndexed { index, item ->
            PrepSectionCard {
                EntryHeader("实习经历 ${index + 1}", onDelete = { onDelete(index) })
                ResumeInput("公司", item.company) { onUpdate(index, item.copy(company = it)) }
                ResumeInput("职位", item.position) { onUpdate(index, item.copy(position = it)) }
                ResumeInput("时间", item.period) { onUpdate(index, item.copy(period = it)) }
                ResumeInput("工作描述", item.description, minLines = 4) {
                    onUpdate(index, item.copy(description = it))
                }
            }
        }
        AddEntryButton("添加实习经历", onAdd)
    }
}

@Composable
private fun ProjectResumeSection(
    draft: ResumeDraft,
    onAdd: () -> Unit,
    onUpdate: (Int, ResumeProject) -> Unit,
    onDelete: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("项目经验", "突出技术选择、个人贡献与验证结果")
        draft.projects.forEachIndexed { index, item ->
            PrepSectionCard {
                EntryHeader("项目 ${index + 1}", onDelete = { onDelete(index) })
                ResumeInput("项目名称", item.name) { onUpdate(index, item.copy(name = it)) }
                ResumeInput("担任角色", item.role) { onUpdate(index, item.copy(role = it)) }
                ResumeInput("时间", item.period) { onUpdate(index, item.copy(period = it)) }
                ResumeInput("项目链接", item.url) { onUpdate(index, item.copy(url = it)) }
                ResumeInput("项目描述", item.description, minLines = 4) {
                    onUpdate(index, item.copy(description = it))
                }
            }
        }
        AddEntryButton("添加项目经验", onAdd)
    }
}

@Composable
private fun SkillsResumeSection(
    value: String,
    onUpdate: (String) -> Unit,
) {
    PrepSectionCard {
        SectionLabel("专业技能", "按语言、框架、工具和数据能力分组表达")
        ResumeInput("技能描述", value, minLines = 6, onValueChange = onUpdate)
    }
}

@Composable
private fun CertificateResumeSection(
    draft: ResumeDraft,
    onAdd: () -> Unit,
    onUpdate: (Int, ResumeCertificate) -> Unit,
    onDelete: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("证书认证", "只保留可核实的证书与成绩")
        draft.certificates.forEachIndexed { index, item ->
            PrepSectionCard {
                EntryHeader("证书 ${index + 1}", onDelete = { onDelete(index) })
                ResumeInput("证书名称", item.name) { onUpdate(index, item.copy(name = it)) }
                ResumeInput("颁发机构", item.issuer) { onUpdate(index, item.copy(issuer = it)) }
                ResumeInput("获得日期", item.period) { onUpdate(index, item.copy(period = it)) }
                ResumeInput("描述", item.description, minLines = 2) {
                    onUpdate(index, item.copy(description = it))
                }
            }
        }
        AddEntryButton("添加证书", onAdd)
    }
}

@Composable
private fun ResumePreview(draft: ResumeDraft) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("resume_preview"),
        shape = RoundedCornerShape(4.dp),
        color = CareerPalette.Surface,
        border = BorderStroke(1.dp, CareerPalette.Border),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = draft.name.ifBlank { "姓名" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = CareerPalette.Ink,
                )
                Text(
                    text = "求职意向：${draft.targetPosition.ifBlank { "待填写" }}",
                    style = MaterialTheme.typography.titleMedium,
                    color = CareerPalette.Moss,
                )
                Text(
                    text = listOf(draft.phone, draft.email, draft.address)
                        .filter(String::isNotBlank)
                        .joinToString(" · ")
                        .ifBlank { "联系方式待填写" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPalette.Muted,
                )
            }
            PreviewSection("个人自评", draft.selfEvaluation)
            PreviewList("教育背景", draft.education.map {
                "${it.school} · ${it.major} · ${it.degree}\n${it.period}\n${it.description}".trim()
            })
            PreviewList("实习经历", draft.experience.map {
                "${it.company} · ${it.position}\n${it.period}\n${it.description}".trim()
            })
            PreviewList("项目经验", draft.projects.map {
                "${it.name} · ${it.role}\n${it.period}\n${it.description}".trim()
            })
            PreviewSection("专业技能", draft.skills)
            PreviewList("证书认证", draft.certificates.map {
                "${it.name} · ${it.issuer} · ${it.period}".trim()
            })
        }
    }
}

@Composable
private fun PreviewSection(title: String, content: String) {
    if (content.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = CareerPalette.Ink,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = CareerPalette.Muted,
        )
    }
}

@Composable
private fun PreviewList(title: String, items: List<String>) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = CareerPalette.Ink,
            fontWeight = FontWeight.SemiBold,
        )
        items.filter(String::isNotBlank).forEach { item ->
            Text(
                text = item,
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Muted,
            )
        }
    }
}

@Composable
private fun EntryHeader(title: String, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = CareerPalette.Ink,
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = "删除$title",
                tint = CareerPalette.Error,
            )
        }
    }
}

@Composable
private fun AddEntryButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null)
        Text(label)
    }
}

@Composable
private fun ResumeInput(
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
        minLines = minLines,
        maxLines = if (minLines > 1) 8 else 2,
        singleLine = minLines == 1,
        shape = RoundedCornerShape(12.dp),
    )
}
