package com.cscareerguide.app.data

import com.cscareerguide.app.data.model.CompanyRanking
import com.cscareerguide.app.data.model.InterviewConfig
import com.cscareerguide.app.data.model.InterviewStyleOption
import com.cscareerguide.app.data.model.JobSummary
import com.cscareerguide.app.data.model.ResumeCertificate
import com.cscareerguide.app.data.model.ResumeDraft
import com.cscareerguide.app.data.model.ResumeEducation
import com.cscareerguide.app.data.model.ResumeExperience
import com.cscareerguide.app.data.model.ResumeProject
import com.cscareerguide.app.data.model.SeniorAdvice

object MockCareerPrepData {
    val jobs = listOf(
        JobSummary(
            id = "job-001",
            title = "Java 后端开发工程师",
            company = "示例科技",
            city = "深圳 · 南山区",
            salary = "15K–25K · 14薪",
            industry = "互联网",
            companySize = "1000–9999人",
            description = "参与招聘平台核心服务开发，负责接口设计、数据建模与稳定性治理。要求掌握 Java、Spring Boot 和 SQL。",
            companyDescription = "示例科技是一家校园招聘模拟企业，所有岗位与公司信息仅用于 UI 演示。",
        ),
        JobSummary(
            id = "job-002",
            title = "前端开发工程师",
            company = "远山数据",
            city = "杭州 · 西湖区",
            salary = "12K–20K · 13薪",
            industry = "企业服务",
            companySize = "500–999人",
            description = "负责数据工作台和可视化页面开发，关注组件抽象、可访问性与移动端体验。熟悉 Vue 3 或 React。",
            companyDescription = "示例岗位，用于展示职位筛选、搜索和申请入口。",
        ),
        JobSummary(
            id = "job-003",
            title = "测试开发工程师",
            company = "青禾软件",
            city = "成都 · 高新区",
            salary = "10K–16K",
            industry = "软件服务",
            companySize = "300–499人",
            description = "搭建接口自动化测试与质量看板，参与需求评审和发布流程。要求具备 Python 或 Java 基础。",
            companyDescription = "示例岗位，仅用于离线 Mock 流程。",
        ),
        JobSummary(
            id = "job-004",
            title = "数据分析工程师",
            company = "北辰零售",
            city = "上海 · 杨浦区",
            salary = "14K–22K · 15薪",
            industry = "零售",
            companySize = "10000人以上",
            description = "负责业务指标体系、SQL 分析和数据看板建设，与产品和运营协作定位增长问题。",
            companyDescription = "示例岗位，不代表真实招聘结果。",
        ),
        JobSummary(
            id = "job-005",
            title = "算法工程实习生",
            company = "星图智能",
            city = "北京 · 海淀区",
            salary = "250–350元/天",
            industry = "人工智能",
            companySize = "100–299人",
            description = "参与文本检索、评测和数据处理，要求熟悉 Python、机器学习基础，有课程项目经验优先。",
            companyDescription = "示例实习岗位，仅用于界面验证。",
        ),
    )

    val redRankings = listOf(
        CompanyRanking(
            id = "red-01",
            rank = 1,
            title = "Java 开发工程师",
            company = "示例科技",
            city = "深圳",
            salary = "18K–30K · 16薪",
            reason = "技术栈清晰，岗位职责明确，薪资与成长空间较完整。",
            isPositive = true,
        ),
        CompanyRanking(
            id = "red-02",
            rank = 2,
            title = "前端开发工程师",
            company = "远山数据",
            city = "杭州",
            salary = "15K–24K · 14薪",
            reason = "业务方向稳定，重视工程质量和跨团队协作。",
            isPositive = true,
        ),
        CompanyRanking(
            id = "red-03",
            rank = 3,
            title = "测试开发工程师",
            company = "青禾软件",
            city = "成都",
            salary = "12K–18K",
            reason = "成长路径明确，适合积累自动化和质量保障经验。",
            isPositive = true,
        ),
    )

    val blackRankings = listOf(
        CompanyRanking(
            id = "black-01",
            rank = 1,
            title = "软件测试",
            company = "示例信息咨询",
            city = "某地",
            salary = "薪资范围异常",
            reason = "岗位名称与描述不一致，要求先缴费，投递前需重点核实招聘主体。",
            isPositive = false,
        ),
        CompanyRanking(
            id = "black-02",
            rank = 2,
            title = "技术助理",
            company = "示例人力服务",
            city = "某地",
            salary = "面议",
            reason = "长期发布同质岗位，职责描述模板化，需确认是否属于外包或派遣。",
            isPositive = false,
        ),
        CompanyRanking(
            id = "black-03",
            rank = 3,
            title = "全栈开发",
            company = "示例网络工作室",
            city = "远程",
            salary = "高薪但要求模糊",
            reason = "低门槛高回报承诺明显，缺少企业主体和正式合同信息。",
            isPositive = false,
        ),
    )

    val initialAdvice = listOf(
        SeniorAdvice(
            id = "advice-001",
            company = "示例科技",
            position = "Java 后端开发",
            author = "林学长",
            graduationYear = "2024",
            rating = 4.5,
            content = "团队代码评审比较细，新人前两个月有明确任务。面试会追问项目中的取舍，不会只问八股。",
            currentCompany = "示例科技",
            salaryInfo = "16K · 14薪",
            pros = listOf("导师制明确", "技术分享固定"),
            cons = listOf("版本发布节奏较快"),
        ),
        SeniorAdvice(
            id = "advice-002",
            company = "远山数据",
            position = "前端开发",
            author = "周学姐",
            graduationYear = "2023",
            rating = 4.0,
            content = "业务和设计协作顺畅，前端有组件库沉淀。准备作品时最好能说明为什么采用当前状态管理方案。",
            currentCompany = "远山数据",
            salaryInfo = "18K · 13薪",
            pros = listOf("设计规范完整", "远程协作工具成熟"),
            cons = listOf("部分项目并行度较高"),
        ),
    )

    val resume = ResumeDraft(
        name = "示例同学",
        targetPosition = "Java 后端开发工程师",
        phone = "138****0000",
        email = "student@example.com",
        address = "长沙",
        selfEvaluation = "计算机专业本科在读，具备 Java、Spring Boot 和 SQL 基础。习惯先明确问题边界，再通过小步迭代和测试验证结果。",
        skills = "Java / Spring Boot / MySQL / Redis / Git\n了解 RESTful API、单元测试和基础部署流程。",
        education = listOf(
            ResumeEducation(
                school = "示例大学",
                major = "计算机科学与技术",
                degree = "本科",
                period = "2023.09–2027.06",
                description = "主修数据结构、操作系统、数据库系统和计算机网络。",
            ),
        ),
        experience = listOf(
            ResumeExperience(
                company = "校园技术中心",
                position = "后端开发实习生",
                period = "2026.03–至今",
                description = "参与课程预约服务接口开发，补充参数校验和接口测试，整理常见错误排查文档。",
            ),
        ),
        projects = listOf(
            ResumeProject(
                name = "课程练习平台",
                role = "后端开发",
                period = "2026.05–2026.08",
                url = "https://example.com/project",
                description = "使用 Spring Boot 和 MySQL 实现账号、题库与练习记录接口，加入统一异常响应和基础自动化测试。",
            ),
        ),
        certificates = listOf(
            ResumeCertificate(
                name = "英语四级",
                issuer = "全国大学英语四六级考试委员会",
                period = "2025.06",
                description = "",
            ),
        ),
    )

    val interviewConfig = InterviewConfig(
        styles = listOf(
            InterviewStyleOption("gentle", "温和面", "循序引导，适合第一次练习和梳理表达。"),
            InterviewStyleOption("technical", "技术面", "围绕项目、基础知识和工程取舍展开。"),
            InterviewStyleOption("pressure", "压力面", "连续追问边界与细节，练习稳定应对。"),
            InterviewStyleOption("behavioral", "行为面", "关注协作、复盘和问题解决过程。"),
        ),
        durationsMinutes = listOf(10, 20, 30, 45),
    )
}
