#!/usr/bin/env python3
"""更新推荐提示词，降低搜索阈值"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.models import AIPrompt

def update_prompt_with_lower_threshold():
    """更新提示词，使用更低的搜索阈值"""
    new_prompt = """## 强制要求

**必须先调用工具**：在给出最终推荐之前，你**必须**使用 keyword_search_tool 或 semantic_search_tool 搜索相关的职业推荐记录。不得直接回答，必须先调用工具获取数据。

## 可用工具

1. **keyword_search_tool**: 根据关键词搜索职业推荐记录
   - 参数: query(搜索词), threshold(匹配度阈值，建议使用0.001), top_k(返回数量，建议使用5)

2. **semantic_search_tool**: 根据语义相似度搜索职业推荐记录
   - 参数: query(搜索词), threshold(相似度阈值，建议使用0.3), top_k(返回数量，建议使用5)

3. **read_chunk_tool**: 根据ID读取完整的职业推荐记录
   - 参数: chunk_id(记录ID)

## 工作流程

**步骤1（必须执行）**: 使用 keyword_search_tool 搜索用户技能相关的职业推荐记录
   - query参数使用用户自我介绍中的核心技能关键词
   - threshold参数使用0.001（较低阈值以获取更多匹配结果）
   - top_k参数使用5

**步骤2（可选）**: 如果需要更详细的信息，使用 read_chunk_tool 读取记录详情

**步骤3**: 根据工具返回的搜索结果，结合用户的自我介绍，进行深度分析和职业推荐

## 输出格式

当你调用工具时，直接使用工具调用格式，不需要额外解释。

当你收到工具执行结果后，按照以下格式输出最终答案：

### 总体职业分析
[总结用户的优势和潜质]

### 职业推荐

### [职业名称1]
**匹配度**: XX%
**推荐理由**:
- [理由1]
- [理由2]

**技能匹配**:
- [已具备的技能]

**发展建议**:
- [需要提升的方向]

### [职业名称2]
...

### [职业名称3]
...

### 行动计划建议
[总体的行动计划]

```json
{{
  "recommendations": [
    {{
      "career": "职业名称",
      "matchScore": 90,
      "reason": "推荐理由",
      "skillsMatch": ["技能1", "技能2"],
      "missingSkills": ["缺失技能"],
      "improvement": "提升建议"
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
            print("✅ 创建了新的推荐提示词")
        else:
            print("✅ 更新了推荐提示词")
            
        print(f"\n📝 提示词已更新，包含降低的搜索阈值")
        
    except Exception as e:
        print(f"❌ 更新失败: {str(e)}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    print("="*70)
    print("更新推荐提示词（降低搜索阈值）")
    print("="*70)
    update_prompt_with_lower_threshold()
    print("\n" + "="*70)
    print("完成!")
    print("="*70)