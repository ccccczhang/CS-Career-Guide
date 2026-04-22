#!/usr/bin/env python3
"""
直接更新 Django admin 中的提示词
"""

import os
import sys
import django

# 设置 Django 环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
django.setup()

from ai_integration.models import AIPrompt

# 新的推荐提示词
new_recommendation_prompt = """你是一个专业的AI职业规划导师。你的任务是根据用户的自我介绍和技能，从提供的职业库中推荐最适合的3个职业，并提供深度的分析。

## 工具使用规则：
1. **首次调用**：收到用户的自我介绍后，必须调用 `keyword_search_tool` 或 `semantic_search_tool` 工具进行搜索
2. **收到工具结果后**：必须直接进行总结，**不得再次调用工具**
3. **总结要求**：根据工具返回的搜索结果，结合职业库信息，给出最终的职业推荐报告

## 输出要求：
1. 语言：必须使用中文进行分析。
2. 格式：
   - 首先给出一段总体的职业分析（overall），总结用户的优势和潜质。
   - 接着为每个推荐的职业提供详细分析：
     - **职业名称**：作为小标题
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
- career: 职业名称
- matchScore: 匹配度数字（0-100）
- reason: 一句话说明适合原因（简洁）

### accordion（用于手风琴展示 - 详细版）：
- overall: 总体职业分析（详细）
- careers: 职业分析数组，每个元素包含：
  - title: 职业名称
  - reasons: 详细推荐理由数组（多条）
  - skills: 用户已具备的技能数组
  - weakestPoint: 当前最大短板
  - marketDemand: 市场热度（高/中/低）
  - salaryRange: 起薪范围（如"15k-25k"）
  - nextStep: 下一步具体建议

注意：JSON中的职业名称必须与详细分析中的职业名称完全一致！
"""

def update_prompt():
    try:
        # 获取或创建推荐提示词
        prompt, created = AIPrompt.objects.get_or_create(
            prompt_type='recommendation',
            defaults={
                'system_prompt': new_recommendation_prompt,
                'user_prompt_template': '{message}',
                'is_active': True
            }
        )
        
        if not created:
            # 更新现有提示词
            prompt.system_prompt = new_recommendation_prompt
            prompt.save()
            print("✅ 成功更新推荐提示词")
        else:
            print("✅ 成功创建推荐提示词")
        
        print(f"\n📋 提示词详情：")
        print(f"类型：{prompt.prompt_type}")
        print(f"状态：{'激活' if prompt.is_active else '未激活'}")
        print(f"提示词长度：{len(prompt.system_prompt)} 字符")
        
    except Exception as e:
        print(f"❌ 更新失败：{str(e)}")
        import traceback
        traceback.print_exc()

if __name__ == '__main__':
    update_prompt()
