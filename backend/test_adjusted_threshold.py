#!/usr/bin/env python3
"""测试调整阈值后的搜索效果"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.langgraph.tools import keyword_search_tool, semantic_search_tool

def test_search_with_adjusted_threshold():
    """测试调整阈值后的搜索效果"""
    print("="*70)
    print("测试调整阈值后的搜索效果")
    print("="*70)
    
    test_queries = [
        "Python 后端开发",
        "Python Django",
        "数据分析",
        "机器学习"
    ]
    
    # 测试不同阈值
    thresholds = [0.001, 0.01, 0.1, 0.5]
    
    for query in test_queries:
        print(f"\n🔍 搜索: '{query}'")
        
        for threshold in thresholds:
            print(f"\n   阈值: {threshold}")
            
            # 关键词搜索
            result = keyword_search_tool.invoke({"query": query, "threshold": threshold, "top_k": 3})
            print(f"   关键词搜索结果: {result.get('count', 0)} 条")
            for r in result.get('results', [])[:2]:
                print(f"     - {r.get('preview', '')[:30]}... (匹配度: {r.get('match_score', 0):.4f})")
    
    # 测试语义搜索
    print("\n" + "="*50)
    print("语义搜索测试")
    print("="*50)
    
    for query in test_queries:
        print(f"\n   语义搜索: '{query}'")
        result = semantic_search_tool.invoke({"query": query, "threshold": 0.3, "top_k": 3})
        print(f"   结果数: {result.get('count', 0)}")
        for r in result.get('results', [])[:2]:
            print(f"     - {r.get('preview', '')[:30]}... (相似度: {r.get('match_score', 0):.4f})")

if __name__ == "__main__":
    test_search_with_adjusted_threshold()