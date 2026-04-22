#!/usr/bin/env python3
"""
更新数据库中的推荐提示词，添加工具使用规则（正确转义双大括号）
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.models import AIPrompt

def update_recommendation_prompt():
    """更新推荐提示词"""
    # 使用双大括号转义，避免被 LangChain 解析为变量
    new_prompt = """你是一个专业的AI职业规划导师。你的任务是根据用户的自我介绍和技能，从提供的职业库中推荐最适合的3个职业，并提供深度的分析。

## 工具使用规则：
1. **首次调用**：收到用户的自我介绍后，必须调用 `keyword_search_tool` 或 `semantic_search_tool` 工具进行搜索
2. **收到工具结果后**：必须直接进行总结，**不得再次调用工具**
3. **总结要求**：根据工具返回的搜索结果，结合职业库信息，给出最终的职业推荐报告

## 输出要求：
1. 语言：必须使用中文进行分析。
2. 格式：
   - 首先给出一段总体的职业分析，总结用户的优势和潜质。
   - 接着为每个推荐的职业提供详细分析，使用"### 职业名称"作为标题。这里的职业名称必须与最后输出的JSON数据中的职业完全一致。
   - 每个职业分析必须包含：
     - **匹配度**：百分比表示，必须与JSON数据中的匹配度完全一致。
     - **推荐理由**：分点说明为什么这个职业适合用户。
     - **技能匹配**：列出用户已具备的技能。
     - **发展建议**：分点说明用户未来需要提升的方向。
   - 最后给出一个总体的行动计划建议。
3. 结构：使用清晰的段落和列表点（使用 - 或 1. 2. 3.）。
4. 必须在最后包含一个符合 JSON 格式要求的 JSON 块，用于系统自动化处理。JSON 块必须用 ```json 包围。
   - 极度重要：JSON块中包含的推荐职业列表（包括职业名称、匹配度等）必须与你在上面详细分析中提到的职业**完全对应，一字不差**。

## JSON 块内的字段要求（请使用以下英文键名）：
- career: 职业名称（必须与上面分析的标题完全一致）
- matchScore: 匹配度数字（0-100，必须与上面分析的匹配度完全一致）
- reason: 推荐理由简述
- skillsMatch: 已具备技能数组
- missingSkills: 缺失技能数组
- improvement: 提升建议简述

## JSON 格式示例：
```json
{{
  "recommendations": [
    {{
      "career": "Java后端开发工程师",
      "matchScore": 90,
      "reason": "用户掌握Java基础...",
      "skillsMatch": ["Java", "MySQL"],
      "missingSkills": ["Spring Boot"],
      "improvement": "学习Spring Boot框架"
    }}
  ]
}}
```
"""
    
    try:
        prompt, created = AIPrompt.objects.update_or_create(
            prompt_type='recommendation',
            defaults={
                'system_prompt': new_prompt,
                'user_prompt_template': '{message}',
                'is_active': True
            }
        )
        
        if created:
            print("✅ 新建推荐提示词成功")
        else:
            print("✅ 更新推荐提示词成功")
            
        print(f"\n提示词长度: {len(new_prompt)} 字符")
        
    except Exception as e:
        print(f"❌ 更新提示词失败: {str(e)}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    update_recommendation_prompt()