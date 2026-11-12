package com.cscareerguide.app.data

import com.cscareerguide.app.data.model.CareerCategory
import com.cscareerguide.app.data.model.SkillGroup
import com.cscareerguide.app.data.model.SkillOption

internal object MockPlanningData {
    val skillGroups = listOf(
        SkillGroup(
            id = "technical",
            title = "技术技能",
            description = "开发语言与工程实践",
            skills = listOf(
                SkillOption("Python 开发", "Python 开发"),
                SkillOption("PostgreSQL 数据库", "PostgreSQL 数据库"),
                SkillOption("RESTfulAPI设计", "RESTful API 设计"),
                SkillOption("并发 / 异步编程", "并发 / 异步编程"),
                SkillOption("Linux 使用与脚本", "Linux 使用与脚本"),
                SkillOption("Git / 版本管理", "Git / 版本管理"),
            ),
        ),
        SkillGroup(
            id = "data",
            title = "数据科学",
            description = "数据分析与人工智能",
            skills = listOf(
                SkillOption("数据分析", "数据分析（Pandas）"),
                SkillOption("机器学习", "机器学习"),
                SkillOption("深度学习框架", "深度学习框架"),
                SkillOption("NLP", "NLP / LLM"),
                SkillOption("CV(图像处理)", "CV（图像处理）"),
                SkillOption("数据可视化(Plotly)", "数据可视化"),
            ),
        ),
        SkillGroup(
            id = "foundation",
            title = "基础技能",
            description = "日常工具与学习能力",
            skills = listOf(
                SkillOption("基本电脑操作", "电脑操作与系统使用"),
                SkillOption("效率管理", "Office 文档与数据处理"),
                SkillOption("效率管理工具:任务管理", "效率工具与任务管理"),
                SkillOption("英文资料阅读能力", "英文资料阅读能力"),
                SkillOption("安全意识意识", "安全意识"),
                SkillOption("学习与问题解决能力", "学习与问题解决能力"),
            ),
        ),
        SkillGroup(
            id = "academic",
            title = "专业知识",
            description = "数学基础与计算机核心课程",
            skills = listOf(
                SkillOption("线性代数与矩阵运算", "线性代数"),
                SkillOption("概率论与统计推断", "概率论"),
                SkillOption("离散数学", "离散数学"),
                SkillOption("数据结构", "数据结构"),
                SkillOption("算法设计", "算法设计"),
                SkillOption("计算机网络", "计算机网络"),
            ),
        ),
    )

    val careerCategories = listOf(
        CareerCategory(
            "backend",
            "后端开发",
            listOf(
                "Java", "C++", "PHP", "Golang", "Python", "Node.js", "Rust",
                "C#工程师", "全栈开发", "安全工程师", "游戏后端", "游戏前端",
                "区块链", "信息技术岗", ".NET", "Delphi", "GIS工程师", "VB",
                "Perl", "Ruby", "Erlang", "后端工程师", "语音视频图形开发",
            ),
        ),
        CareerCategory(
            "ai",
            "人工智能",
            listOf(
                "算法工程师", "深度学习", "自然语言处理", "机器学习",
                "计算机视觉", "语音识别", "推荐算法", "数据挖掘",
            ),
        ),
        CareerCategory(
            "frontend",
            "前端开发",
            listOf("Web前端", "前端工程师", "游戏前端", "H5开发", "React/Vue", "小程序开发"),
        ),
        CareerCategory(
            "client",
            "客户端开发",
            listOf("安卓 (Android)", "iOS开发", "Flutter开发", "Unity3D", "游戏客户端"),
        ),
        CareerCategory(
            "data",
            "数据",
            listOf("数据分析师", "大数据开发", "数据仓库 (ETL)", "数据架构师"),
        ),
        CareerCategory(
            "operations",
            "运维",
            listOf("运维工程师", "网络工程师", "安全运维", "SRE 工程师", "DBA (数据库管理)"),
        ),
        CareerCategory(
            "testing",
            "测试",
            listOf("测试工程师", "自动化测试", "性能测试", "测试开发 (TestDev)"),
        ),
        CareerCategory(
            "engineering",
            "研发工程师",
            listOf("嵌入式软件开发", "嵌入式硬件开发", "固件开发", "FPGA/芯片设计"),
        ),
        CareerCategory(
            "support",
            "销售技术支持",
            listOf("售前技术工程师", "客户成功", "售后技术支持", "技术支持经理"),
        ),
    )
}
