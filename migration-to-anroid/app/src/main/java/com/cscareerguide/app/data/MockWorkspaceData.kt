package com.cscareerguide.app.data

import com.cscareerguide.app.data.model.CatalogItem
import com.cscareerguide.app.data.model.CatalogKind
import com.cscareerguide.app.data.model.EvaluationMetrics
import com.cscareerguide.app.data.model.EvaluationRun
import com.cscareerguide.app.data.model.PracticeAnswer
import com.cscareerguide.app.data.model.PracticeQuestion
import com.cscareerguide.app.data.model.PracticeRecordDetail
import com.cscareerguide.app.data.model.ResumeAnalysis
import com.cscareerguide.app.data.model.ResumeSuggestion

object MockWorkspaceData {
    val catalog = listOf(
        CatalogItem(
            id = "catalog-job-001",
            kind = CatalogKind.JOBS,
            title = "Java 后端开发工程师",
            subtitle = "示例科技 · 应届生",
            city = "深圳",
            role = "Java后端",
            tags = listOf("Spring Boot", "MySQL", "校招"),
            description = "负责招聘业务接口、数据建模与稳定性治理。",
            source = "团队创作：示例招聘数据",
            collectedAt = "2026-11-12",
        ),
        CatalogItem(
            id = "catalog-job-002",
            kind = CatalogKind.JOBS,
            title = "前端开发工程师",
            subtitle = "远山数据 · 1年以内",
            city = "杭州",
            role = "前端",
            tags = listOf("Vue 3", "TypeScript", "可视化"),
            description = "负责数据工作台、组件抽象和移动端体验。",
            source = "团队创作：示例招聘数据",
            collectedAt = "2026-11-12",
        ),
        CatalogItem(
            id = "catalog-company-001",
            kind = CatalogKind.COMPANIES,
            title = "示例科技",
            subtitle = "互联网 · 1000–9999人",
            city = "深圳",
            tags = listOf("企业服务", "校园招聘"),
            description = "离线演示企业，不代表真实招聘主体。",
            source = "团队创作：示例企业数据",
            collectedAt = "2026-11-12",
        ),
        CatalogItem(
            id = "catalog-company-002",
            kind = CatalogKind.COMPANIES,
            title = "远山数据",
            subtitle = "企业服务 · 500–999人",
            city = "杭州",
            tags = listOf("数据平台", "企业服务"),
            description = "离线演示企业，用于核对目录筛选和来源展示。",
            source = "团队创作：示例企业数据",
            collectedAt = "2026-11-12",
        ),
        CatalogItem(
            id = "catalog-question-001",
            kind = CatalogKind.QUESTIONS,
            title = "ArrayList 与 LinkedList 有哪些主要差异？",
            subtitle = "Java基础 · 基础",
            role = "Java后端",
            topic = "集合",
            difficulty = "easy",
            tags = listOf("集合", "复杂度"),
            keyPoints = listOf(
                "ArrayList 基于动态数组，随机访问快。",
                "LinkedList 基于双向链表，按索引访问需要遍历。",
                "选择时还要考虑内存开销和实际读写模式。",
            ),
            source = "团队创作：自编虚构题",
            collectedAt = "2026-11-12",
        ),
        CatalogItem(
            id = "catalog-question-002",
            kind = CatalogKind.QUESTIONS,
            title = "数据库索引为什么可能不生效？",
            subtitle = "数据库 · 进阶",
            role = "Java后端",
            topic = "数据库",
            difficulty = "medium",
            tags = listOf("SQL", "索引"),
            keyPoints = listOf(
                "查询条件和索引列不匹配。",
                "对索引列做函数计算或隐式类型转换。",
                "联合索引不满足最左前缀。",
            ),
            source = "团队创作：自编虚构题",
            collectedAt = "2026-11-12",
        ),
        CatalogItem(
            id = "catalog-question-003",
            kind = CatalogKind.QUESTIONS,
            title = "TCP 为什么需要三次握手？",
            subtitle = "计算机网络 · 基础",
            role = "通用",
            topic = "网络",
            difficulty = "easy",
            tags = listOf("TCP", "握手"),
            keyPoints = listOf(
                "双方确认收发能力。",
                "同步初始序列号。",
                "避免历史连接请求造成错误连接。",
            ),
            source = "团队创作：自编虚构题",
            collectedAt = "2026-11-12",
        ),
    )

    val resumeAnalysis = ResumeAnalysis(
        id = "analysis-001",
        resumeRevision = 3,
        summary = "找到 3 处可以补充证据的表达。确认前不会修改当前简历。",
        suggestions = listOf(
            ResumeSuggestion(
                id = "suggestion-001",
                section = "项目经验",
                original = "参与课程预约服务接口开发",
                evidence = "目标 JD 强调接口设计、测试和稳定性。",
                suggestion = "补充你负责的接口数量、参数校验方式和自动化测试结果。",
            ),
            ResumeSuggestion(
                id = "suggestion-002",
                section = "专业技能",
                original = "了解 RESTful API、单元测试",
                evidence = "目标 JD 要求能够独立交付后端服务。",
                suggestion = "将“了解”改成能通过项目或测试证明的实际使用程度。",
            ),
            ResumeSuggestion(
                id = "suggestion-003",
                section = "个人自评",
                original = "习惯先明确问题边界",
                evidence = "该表达缺少行为或结果证据。",
                suggestion = "补充一次明确边界后减少返工的具体经历。",
            ),
        ),
    )

    val practiceQuestions = listOf(
        PracticeQuestion(
            id = "practice-q-001",
            question = "ArrayList 扩容机制是什么？如何选择初始容量？",
            role = "Java后端",
            topic = "集合",
            difficulty = "easy",
            keyPoints = listOf(
                "默认容量与扩容倍率。",
                "扩容会复制数组，产生时间和内存成本。",
                "可根据已知数据规模预设容量。",
            ),
            sourceEvidence = listOf(
                "source: 团队创作：自编虚构题",
                "topic: 集合",
            ),
            matchScore = 0.923,
        ),
        PracticeQuestion(
            id = "practice-q-002",
            question = "ConcurrentHashMap 如何保证并发安全？",
            role = "Java后端",
            topic = "并发",
            difficulty = "medium",
            keyPoints = listOf(
                "避免对整个容器加单一全局锁。",
                "说明原子更新和可见性保障。",
                "区分 ConcurrentHashMap 与同步包装 Map 的适用场景。",
            ),
            sourceEvidence = listOf(
                "source: 团队创作：自编虚构题",
                "topic: 并发",
            ),
            matchScore = 0.816,
        ),
        PracticeQuestion(
            id = "practice-q-003",
            question = "联合索引的最左前缀原则如何影响查询？",
            role = "Java后端",
            topic = "数据库",
            difficulty = "hard",
            keyPoints = listOf(
                "联合索引按定义顺序组织。",
                "范围条件可能影响后续列的使用。",
                "通过执行计划和数据分布验证。",
            ),
            sourceEvidence = listOf(
                "source: 团队创作：自编虚构题",
                "topic: 数据库",
            ),
            matchScore = 0.742,
        ),
    )

    val records = listOf(
        PracticeRecordDetail(
            id = "record-001",
            title = "Java 后端岗位题库练习",
            role = "Java后端",
            topic = "集合",
            difficulty = "easy",
            status = "ended",
            createdAt = "2026-11-12 15:20",
            summaryStatus = "succeeded",
            strengths = listOf("能说明扩容成本", "回答结构清楚"),
            gaps = listOf("初始容量选择缺少场景", "没有补充复杂度比较"),
            nextSteps = listOf("补充一次真实数据规模估算", "练习集合选型追问"),
            answers = listOf(
                PracticeAnswer(
                    id = "answer-001",
                    question = "ArrayList 扩容机制是什么？",
                    answer = "底层数组满后按一定倍率扩容，并通过复制数组迁移元素。",
                    coveredPoints = listOf("扩容倍率", "复制成本"),
                    missingPoints = listOf("未说明默认容量"),
                    followUp = "如果已知需要存储一万条数据，你会如何选择初始容量？",
                ),
            ),
        ),
        PracticeRecordDetail(
            id = "record-002",
            title = "数据库索引专项练习",
            role = "Java后端",
            topic = "数据库",
            difficulty = "medium",
            status = "ended",
            createdAt = "2026-11-10 10:45",
            summaryStatus = "succeeded",
            strengths = listOf("能识别函数导致索引失效", "会提及执行计划"),
            gaps = listOf("最左前缀解释还可以更具体"),
            nextSteps = listOf("用联合索引示例重讲一次", "补充分页深偏移问题"),
            answers = listOf(
                PracticeAnswer(
                    id = "answer-002",
                    question = "哪些写法会导致索引失效？",
                    answer = "例如对索引列使用函数、隐式类型转换，或者联合索引不满足最左前缀。",
                    coveredPoints = listOf("函数计算", "最左前缀"),
                    missingPoints = listOf("隐式转换的具体场景"),
                    followUp = "执行计划显示扫描行数很低就是一定没有问题吗？",
                ),
            ),
        ),
        PracticeRecordDetail(
            id = "record-003",
            title = "并发编程练习",
            role = "Java后端",
            topic = "并发",
            difficulty = "hard",
            status = "ended",
            createdAt = "2026-11-08 20:15",
            summaryStatus = "failed",
            strengths = listOf("回答已保存"),
            gaps = listOf("总结生成失败，可在桥接后重试"),
            nextSteps = listOf("恢复模型服务后重新生成总结"),
            answers = emptyList(),
        ),
    )

    val evaluations = listOf(
        EvaluationRun(
            id = "eval-001",
            task = "question",
            datasetVersion = "java-eval-test-v1",
            strategy = "text-baseline",
            topK = 5,
            createdAt = "2026-11-12 16:30",
            sampleCount = 40,
            failureCount = 0,
            modelVersion = "mock-text-baseline-v1",
            metrics = EvaluationMetrics(
                precisionAtK = 0.486,
                recallAtK = 0.742,
                f1AtK = 0.587,
                hitRateAtK = 0.825,
                mrr = 0.701,
            ),
        ),
        EvaluationRun(
            id = "eval-002",
            task = "question",
            datasetVersion = "java-eval-dev-v1",
            strategy = "text-balanced",
            topK = 5,
            createdAt = "2026-11-11 14:10",
            sampleCount = 36,
            failureCount = 0,
            modelVersion = "mock-text-balanced-v1",
            metrics = EvaluationMetrics(
                precisionAtK = 0.512,
                recallAtK = 0.768,
                f1AtK = 0.614,
                hitRateAtK = 0.847,
                mrr = 0.729,
            ),
        ),
    )
}
