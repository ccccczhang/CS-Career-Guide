#!/usr/bin/env python3
"""全面测试职业推荐功能的关键词匹配与语义匹配"""

import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'backend'))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.arag_agent import get_arag_agent
from ai_integration.arag_integration import ARAGCareerSearch

def test_career_recommendation():
    print("=" * 70)
    print("全面测试职业推荐功能")
    print("=" * 70)
    
    # 获取代理和搜索器
    agent = get_arag_agent()
    search = ARAGCareerSearch()
    
    # 测试案例
    test_cases = [
        {
            "name": "网络安全方向",
            "query": "我对网络安全很感兴趣，想从事渗透测试相关工作",
            "expected_keywords": ["网络安全", "渗透测试"]
        },
        {
            "name": "前端开发方向",
            "query": "我擅长HTML、CSS和JavaScript，想做网页开发",
            "expected_keywords": ["前端", "HTML", "CSS", "JavaScript"]
        },
        {
            "name": "Python数据分析方向",
            "query": "我精通Python，熟悉Pandas和数据分析",
            "expected_keywords": ["Python", "数据分析", "Pandas"]
        }
    ]
    
    # 测试关键词搜索
    print("\n" + "=" * 70)
    print("1. 测试关键词搜索")
    print("=" * 70)
    
    for case in test_cases:
        print(f"\n测试案例: {case['name']}")
        print(f"查询词: {case['query']}")
        
        # 测试关键词搜索
        keyword_result = agent.keyword_search(case['query'], limit=3)
        print(f"关键词搜索 - 找到 {keyword_result['count']} 条结果")
        
        if keyword_result['results']:
            for i, r in enumerate(keyword_result['results'], 1):
                print(f"  {i}. {r['preview'][:50]}... (匹配度: {r.get('match_score', 0):.2f})")
        else:
            print("  未找到匹配结果")
    
    # 测试语义搜索
    print("\n" + "=" * 70)
    print("2. 测试语义搜索")
    print("=" * 70)
    
    for case in test_cases:
        print(f"\n测试案例: {case['name']}")
        print(f"查询词: {case['query']}")
        
        # 测试语义搜索
        semantic_result = agent.semantic_search(case['query'], limit=3)
        print(f"语义搜索 - 找到 {semantic_result['count']} 条结果")
        
        if semantic_result['results']:
            for i, r in enumerate(semantic_result['results'], 1):
                match_type = r.get('match_type', 'semantic') if semantic_result.get('success') else 'keyword_fallback'
                print(f"  {i}. {r['preview'][:50]}... (相似度: {r.get('match_score', 0):.4f}, 类型: {match_type})")
        else:
            print("  未找到匹配结果")
    
    # 测试完整推荐
    print("\n" + "=" * 70)
    print("3. 测试完整职业推荐")
    print("=" * 70)
    
    for case in test_cases:
        print(f"\n测试案例: {case['name']}")
        print(f"自我介绍: {case['query']}")
        
        # 调用完整推荐
        recommendations = agent.agent_recommend(case['query'])
        source = recommendations.get('source', 'unknown')
        
        print(f"推荐来源: {source}")
        print(f"推荐职业数量: {len(recommendations.get('recommendations', []))}")
        
        if recommendations.get('recommendations'):
            for i, rec in enumerate(recommendations['recommendations'][:3], 1):
                print(f"  {i}. {rec['career']} (匹配度: {rec['matchScore']})")
                if 'reason' in rec:
                    print(f"     推荐理由: {rec['reason'][:50]}...")
        
        # 检查是否使用了参考案例
        if recommendations.get('reference_cases') and len(recommendations['reference_cases']) > 0:
            print(f"  ✅ 使用了 {len(recommendations['reference_cases'])} 个参考案例")
        else:
            print(f"  ⚠️ 未使用参考案例（可能没有匹配的历史记录）")
    
    # 测试混合搜索
    print("\n" + "=" * 70)
    print("4. 测试混合搜索")
    print("=" * 70)
    
    test_query = "我想做技术相关的工作"
    print(f"\n测试查询: '{test_query}'")
    
    hybrid_results = search.hybrid_search(test_query, top_k=3)
    print(f"混合搜索 - 找到 {len(hybrid_results)} 条结果")
    
    if hybrid_results:
        for i, r in enumerate(hybrid_results, 1):
            print(f"  {i}. {r.get('introduction_preview', '')[:50]}...")
            print(f"     匹配类型: {r.get('match_type', 'unknown')}")
            print(f"     匹配度: {r.get('match_score', 0):.4f}")
    
    print("\n" + "=" * 70)
    print("测试完成！")
    print("=" * 70)

if __name__ == '__main__':
    test_career_recommendation()