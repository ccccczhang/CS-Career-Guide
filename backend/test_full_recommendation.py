import os
import time

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
import django
django.setup()

from ai_integration.optimized_views import _generate_llm_recommendation

print("="*60)
print("🧪 测试完整的LangGraph + A-RAG职业推荐")
print("="*60)

self_introduction = """姓名: 张三
学校: 长沙理工大学
专业: 计算机科学
年级: 大三
性别: 男
技能: Python开发、数据结构、数据库设计
职业期望: 软件工程师"""

print(f"\n用户自我介绍:\n{self_introduction}")
print("\n" + "="*60)

start = time.time()
try:
    result = _generate_llm_recommendation(self_introduction)
    elapsed = time.time() - start
    
    print(f"\n✅ 职业推荐生成成功，耗时: {elapsed:.2f}秒")
    print(f"\n推荐结果 ({len(result['recommendations'])} 个):")
    for i, rec in enumerate(result['recommendations'], 1):
        print(f"\n{i}. {rec.get('career', '未知职业')}")
        print(f"   匹配度: {rec.get('matchScore', 0)}")
        print(f"   推荐理由: {rec.get('reason', '')}")
    
    if result.get('analysis_result'):
        print(f"\n分析结果:\n{result['analysis_result'][:200]}...")
        
except Exception as e:
    elapsed = time.time() - start
    print(f"\n❌ 职业推荐失败，耗时: {elapsed:.2f}秒")
    print(f"错误信息: {e}")
    import traceback
    traceback.print_exc()

print("\n" + "="*60)
print("测试完成")
print("="*60)