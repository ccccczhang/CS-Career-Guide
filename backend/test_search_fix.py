#!/usr/bin/env python3
"""
测试修复后的搜索功能
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.arag_integration import ARAGCareerSearch

def test_searches():
    """测试关键词搜索和语义搜索"""
    print("\n" + "="*60)
    print("🚀 测试修复后的搜索功能")
    print("="*60)
    
    # 创建搜索器实例
    search = ARAGCareerSearch()
    
    # 测试查询
    query = "go Java python"
    
    # 测试关键词搜索
    print("\n📌 测试关键词搜索")
    print("-" * 40)
    try:
        results = search.keyword_search(query, threshold=0.7, top_k=3)
        print(f"✅ 关键词搜索成功")
        print(f"   找到 {len(results)} 条匹配记录")
        for i, r in enumerate(results):
            print(f"   {i+1}. ID: {r.get('id')}, 匹配度: {r.get('match_score', 0):.4f}")
    except Exception as e:
        print(f"❌ 关键词搜索失败: {str(e)}")
        import traceback
        traceback.print_exc()
    
    # 测试语义搜索（模型加载可能较慢）
    print("\n📌 测试语义搜索")
    print("-" * 40)
    try:
        results = search.semantic_search(query, threshold=0.5, top_k=3)
        print(f"✅ 语义搜索成功")
        print(f"   找到 {len(results)} 条匹配记录")
        for i, r in enumerate(results):
            print(f"   {i+1}. ID: {r.get('id')}, 相似度: {r.get('match_score', 0):.4f}")
    except Exception as e:
        print(f"❌ 语义搜索失败: {str(e)}")
        print("   (可能是模型未下载或网络问题)")

if __name__ == "__main__":
    test_searches()