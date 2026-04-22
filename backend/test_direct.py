import os
import sys
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')

import django
django.setup()

from ai_integration.optimized_views import _simple_llm_recommendation

print('直接测试 _simple_llm_recommendation 函数...')

result = _simple_llm_recommendation('姓名: 张三\n学校: 长沙理工大学\n专业: 计算机科学\n年级: 大三\n性别: 男\n技能: Python开发、数据结构\n职业期望: 软件工程师')

print(f'推荐数量: {len(result.get("recommendations", []))}')
for i, rec in enumerate(result.get("recommendations", []), 1):
    print(f'  {i}. {rec.get("career")} (匹配度: {rec.get("matchScore")})')
    print(f'     理由: {rec.get("reason")}')
print(f'分析结果: {result.get("analysis_result", "")[:100]}...')