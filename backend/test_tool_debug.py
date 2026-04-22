#!/usr/bin/env python3
"""
测试工具调用调试信息输出
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.langgraph.tools import keyword_search_tool

def test_tool_debug():
    """测试工具调用调试信息"""
    print("\n" + "="*60)
    print("🚀 测试工具调用调试信息")
    print("="*60)
    
    # 直接调用工具测试
    print("\n📌 测试关键词搜索工具")
    print("-" * 40)
    
    result = keyword_search_tool.invoke({
        "query": "Java",
        "threshold": 0.7,
        "top_k": 3
    })
    
    print(f"\n✅ 工具执行结果:")
    print(f"   成功: {result.get('success')}")
    print(f"   数量: {result.get('count')}")
    print(f"   查询词: {result.get('query')}")
    
    if result.get('results'):
        print(f"\n📋 匹配结果预览:")
        for i, r in enumerate(result['results'][:2]):
            print(f"   {i+1}. ID: {r.get('id')}, 匹配度: {r.get('match_score'):.4f}")

if __name__ == "__main__":
    test_tool_debug()