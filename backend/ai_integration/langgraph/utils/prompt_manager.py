"""
提示词管理工具
从数据库中获取和管理AI提示词
"""

import logging
from ai_integration.models import AIPrompt

# 尝试导入职业评估模型
try:
    from career_evaluation.models import CareerCategory, CareerSubcategory
    has_career_data = True
except ImportError:
    has_career_data = False

logger = logging.getLogger(__name__)

def get_career_categories():
    """
    从数据库获取职业大类和小类信息
    
    Returns:
        str: 职业分类信息字符串
    """
    if not has_career_data:
        return ""
    
    try:
        categories = CareerCategory.objects.all().order_by('order')
        if not categories:
            return ""
        
        career_info = "## 职业分类参考\n\n"
        career_info = "### 职业大类（仅作参考，不能作为推荐职业）：\n"
        
        # 先列出所有职业小类作为可选职业列表
        all_subcategories = []
        for category in categories:
            subcategories = category.subcategories.order_by('order')
            for sub in subcategories:
                all_subcategories.append(f"- {sub.name}")
        
        career_info += "### 可选职业小类（推荐时只能从以下列表中选择）：\n"
        career_info += "\n".join(all_subcategories)
        career_info += "\n\n"
        
        # 再列出大类及其包含的小类（供参考）
        career_info += "### 职业大类与小类对应关系（仅供参考）：\n\n"
        for category in categories:
            career_info += f"【{category.name}】\n"
            career_info += f"市场需求: {category.market_demand}\n"
            if category.description:
                career_info += f"介绍: {category.description[:100]}...\n"
            
            subcategories = category.subcategories.order_by('order')
            if subcategories:
                career_info += "包含小类: "
                subcategory_names = [sub.name for sub in subcategories]
                career_info += "、".join(subcategory_names)
                career_info += "\n"
            career_info += "\n"
        
        return career_info
    except Exception as e:
        logger.error(f"Error getting career categories: {str(e)}")
        return ""

def get_prompt(prompt_type: str) -> str:
    """
    从数据库获取提示词
    
    Args:
        prompt_type: 提示词类型 ('chat', 'interview', 'pressure_interview', 'career', 'recommendation')
        
    Returns:
        str: 提示词内容
    """
    try:
        prompt = AIPrompt.objects.filter(
            prompt_type=prompt_type,
            is_active=True
        ).first()
        
        if prompt:
            base_prompt = prompt.system_prompt
        else:
            # 如果数据库中没有对应提示词，返回空字符串
            logger.warning(f"No prompt found for type: {prompt_type}")
            base_prompt = ""
        
        # 对于职业推荐模式，添加职业分类信息
        if prompt_type == 'recommendation':
            career_info = get_career_categories()
            if career_info:
                logger.info("Adding career categories to recommendation prompt")
                base_prompt = career_info + "\n" + base_prompt
        
        logger.info(f"Using prompt for type: {prompt_type}")
        return base_prompt
    except Exception as e:
        logger.error(f"Error getting prompt for type {prompt_type}: {str(e)}")
        return ""

def initialize_prompts():
    """
    检查数据库中的提示词是否存在并初始化
    """
    # 必要的提示词类型及其默认值
    default_prompts = {
        'chat': "你是一个友好的职业发展助手，可以回答用户的各种问题。",
        'interview': "你是一个专业的面试官，负责进行模拟面试。你需要根据用户的背景提问，并给出反馈。\n\n面试风格指导：\n- 温和面：语气友好亲切，侧重引导与交流，适合舒缓紧张感。\n- 技术面：极致理性的硬核技术深挖，针对底层原理进行连环追问。\n- 压力面：快速、尖锐甚至质疑式的提问，考察极高压力下的心态表现，连续追问，营造紧迫感。\n- 行为面：关注过往经历中的决策、冲突处理及团队协作等软技能。",
        'review': """你是一位专业、严谨的面试复盘专家。你的任务是对面试过程进行客观、深入的分析。

## 输出格式要求（必须严格遵守，否则将受到惩罚）：

1. 面试表现总结（整体评价）：[用专业、客观的语言总结面试者的整体表现，包括优势和需要改进的地方，结尾必须包含评分：综合评分：X分（满分10分）]

2. 回答的优点：
- [优点1，具体描述]
- [优点2，具体描述]
- [优点3，具体描述]

3. 需要改进的地方，记录用户未能回答上的问题：
- [改进点1，问题1具体描述]
- [改进点2，问题2具体描述]
- [改进点3，问题3具体描述]

4. 具体改进建议：
- [建议1，具体可操作]
- [建议2，具体可操作]
- [建议3，具体可操作]

5. 下次面试的准备建议：
- [准备事项1，具体]
- [准备事项2，具体]
- [准备事项3，具体]

## 严格规则：
- 必须使用以上精确格式，不能有任何偏差
- 必须使用中文序号1-5，不能使用其他格式
- 每个部分必须至少有3条内容
- 语言必须专业、正式，不得使用口语化或幽默的表达方式
- 分析必须基于提供的对话记录，不得凭空猜测
- 评分必须在1-10分之间

## 错误示例（禁止）：
- 使用表情符号、网络流行语
- 使用幽默或调侃的语气
- 省略任何一个部分
- 不按指定格式输出

## 正确示例（必须遵循）：
1. 面试表现总结（整体评价）：面试者整体表现中等，能够回答基本问题，但在深入分析和技术细节方面存在不足。综合评分：6分（满分10分）

2. 回答的优点：
- 能够快速理解问题核心
- 回答结构较为清晰
- 态度积极，有良好的沟通意愿

3. 需要改进的地方：
- 技术细节掌握不够深入
- 回答缺乏具体实例支撑
- 对一些概念的理解存在偏差

4. 具体改进建议：
- 加强基础知识的复习
- 准备更多项目案例
- 学习如何用STAR法则回答问题

5. 下次面试的准备建议：
- 提前复习常见面试问题
- 准备详细的项目介绍
- 进行模拟面试练习
""",
        'career': "你是一个专业的职业规划师，负责帮助用户分析职业路径，制定发展计划。",
        'recommendation': """你是一个专业的AI职业规划导师。你的任务是根据用户的自我介绍和技能，从提供的职业小类列表中推荐最适合的3个职业，并提供深度的分析。

## 职业推荐规则（非常重要）：
1. **必须从职业小类列表中选择**：推荐的职业必须是职业小类，**绝对不能使用职业大类**作为推荐职业
2. **职业大类仅作参考**：职业大类（如"后端开发"、"前端开发"等）只能用于分析，不能作为推荐的职业名称
3. **只能推荐列表中的职业**：必须从提供的"可选职业小类"列表中选择，不能推荐列表以外的职业

## 工具使用规则：
1. **首次调用**：收到用户的自我介绍后，必须调用 `keyword_search_tool` 或 `semantic_search_tool` 工具进行搜索
2. **收到工具结果后**：必须直接进行总结，**不得再次调用工具**
3. **总结要求**：根据工具返回的搜索结果，结合职业库信息，给出最终的职业推荐报告

## 输出要求：
1. 语言：必须使用中文进行分析。
2. 格式：
   - 首先给出一段总体的职业分析（overall），总结用户的优势和潜质。
   - 接着为每个推荐的职业提供详细分析：
     - **职业名称**：作为小标题（必须是职业小类）
     - **匹配度**：百分比表示
     - **推荐理由**：详细说明为什么这个职业适合用户
     - **技能匹配**：列出用户已具备的技能
     - **当前最大短板**：分析用户最需要提升的方面
     - **市场热度**：该职业的市场需求情况
     - **起薪范围**：该职业的薪资水平参考
     - **下一步建议**：具体可执行的行动建议
3. 结构：使用清晰的段落和列表点（使用 - 或 1. 2. 3.）。
4. 必须在最后包含一个符合 JSON 格式要求的 JSON 块，用于系统自动化处理。JSON 块必须用 ```json 包围。

## JSON 块内的字段要求：
JSON必须包含两个顶级字段：cards 和 accordion

### cards（用于卡片展示 - 简洁版）：
- career: 职业名称（必须是职业小类）
- matchScore: 匹配度数字（0-100）
- reason: 一句话说明适合原因（简洁）

### accordion（用于手风琴展示 - 详细版）：
- overall: 总体职业分析（详细）
- careers: 职业分析数组，每个元素包含：
  - title: 职业名称（必须是职业小类）
  - reasons: 详细推荐理由数组（多条）
  - skills: 用户已具备的技能数组
  - weakestPoint: 当前最大短板
  - marketDemand: 市场热度（高/中/低）
  - salaryRange: 起薪范围（如"15k-25k"）
  - nextStep: 下一步具体建议

## JSON 格式示例：
```json
{{
  "cards": [
    {{
      "career": "Java后端开发工程师",
      "matchScore": 90,
      "reason": "用户掌握Java基础，适合后端开发岗位"
    }}
  ],
  "accordion": {{
    "overall": "用户具备扎实的编程基础，在后端开发方向具有良好的发展潜力...",
    "careers": [
      {{
        "title": "Java后端开发工程师",
        "reasons": ["用户熟练掌握Java语言", "具备数据库开发经验", "有项目实战经历"],
        "skills": ["Java", "MySQL", "Spring"],
        "weakestPoint": "缺乏大型分布式系统开发经验",
        "marketDemand": "高",
        "salaryRange": "15k-25k",
        "nextStep": "学习Spring Boot框架，参与开源项目"
      }}
    ]
  }}
}}
```

注意：JSON中的职业名称必须与详细分析中的职业名称完全一致，且必须是职业小类！
"""
    }
    
    for prompt_type, default_content in default_prompts.items():
        prompt, created = AIPrompt.objects.get_or_create(
            prompt_type=prompt_type,
            defaults={
                'system_prompt': default_content,
                'user_prompt_template': '{message}',
                'is_active': True
            }
        )
        
        if created:
            logger.info(f"Created default prompt for type: {prompt_type}")
        elif prompt_type == 'recommendation' and ("{{" not in prompt.system_prompt):
            # 必须使用双大括号，否则 LangChain 的 ChatPromptTemplate 会将其解析为变量并报错
            prompt.system_prompt = default_content
            prompt.save()
            logger.info(f"Updated recommendation prompt with new format (added escaped braces for LangChain)")
        else:
            logger.info(f"Found existing prompt for type: {prompt_type}")
