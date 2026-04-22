#!/usr/bin/env python3
"""对比关键词匹配和语义匹配的差异"""

import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'backend'))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.arag_integration import ARAGCareerSearch

def compare_matching():
    search = ARAGCareerSearch()
    
    test_cases = [
        "网络安全",
        "渗透测试",
        "我对网络安全很感兴趣",
        "想从事渗透测试相关工作",
        "热爱前端开发",
        "Python数据分析"
    ]
    
    print("=" * 70)
    print("关键词匹配 vs 语义匹配对比")
    print("=" * 70)
    
    for query in test_cases:
        print(f"\n查询: '{query}'")
        print("-" * 50)
        
        # 关键词搜索
        keyword_results = search.keyword_search(query, threshold=0.7)
        print(f"关键词匹配: {len(keyword_results)} 条结果")
        
        # 语义搜索
        semantic_results = search.semantic_search(query, threshold=0.5)
        print(f"语义匹配: {len(semantic_results)} 条结果")
        
        if semantic_results:
            print(f"   最高相似度: {max(r['match_score'] for r in semantic_results):.4f}")

if __name__ == '__main__':
    compare_matching()