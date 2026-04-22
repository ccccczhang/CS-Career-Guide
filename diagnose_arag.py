#!/usr/bin/env python3
"""诊断 A-RAG 数据存储和读取问题"""

import os
import sys

# 添加项目路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'backend'))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.arag_integration import ARAGCareerSearch

def diagnose_lancedb():
    print("=" * 70)
    print("诊断 LanceDB 数据存储和读取问题")
    print("=" * 70)
    
    # 创建搜索器
    search = ARAGCareerSearch()
    print(f"\n1. 数据库路径: {search.db_path}")
    print(f"   表名: {search.table_name}")
    print(f"   表列表: {search.db.table_names()}")
    
    # 检查表是否存在
    if search.table_name in search.db.table_names():
        print("   ✅ 表存在")
        
        # 尝试直接读取表数据
        try:
            df = search.table.to_pandas()
            print(f"\n2. 表记录数: {len(df)}")
            if len(df) > 0:
                print("   ✅ 表中有数据")
                print("   字段列表:", df.columns.tolist())
                
                # 显示前3条记录
                print("\n3. 前3条记录预览:")
                for i in range(min(3, len(df))):
                    record = df.iloc[i]
                    print(f"\n   记录{i+1}:")
                    print(f"     id: {record.get('id', 'N/A')}")
                    print(f"     self_introduction: {str(record.get('self_introduction', ''))[:50]}...")
                    print(f"     introduction_preview: {str(record.get('introduction_preview', ''))[:50]}...")
                    print(f"     recommendations: {str(record.get('recommendations', ''))[:80]}...")
            else:
                print("   ❌ 表为空")
        except Exception as e:
            print(f"   ❌ 读取表数据失败: {str(e)}")
    else:
        print("   ❌ 表不存在")
    
    # 测试新的中文分词方法
    print("\n" + "=" * 70)
    print("测试新的中文分词方法")
    print("=" * 70)
    
    test_query = "Python 数据分析"
    print(f"\n测试查询: '{test_query}'")
    
    # 测试分词
    query_tokens = search._tokenize_chinese(test_query)
    print(f"分词结果: {query_tokens}")
    print(f"分词集合: {set(query_tokens)}")
    
    # 测试每条记录的分词
    print("\n测试记录分词:")
    results = search.table.to_pandas().to_dict('records')
    
    for i, result in enumerate(results):
        content = result.get("introduction_preview", result.get("self_introduction", ""))
        record_tokens = search._tokenize_chinese(content)
        print(f"\n  记录{i+1}预览: {content[:30]}...")
        print(f"  分词结果: {record_tokens[:10]}...")
        
        # 计算匹配
        common_words = set(query_tokens).intersection(set(record_tokens))
        print(f"  匹配词: {common_words}")
        
        if common_words:
            match_score = len(common_words) / len(set(query_tokens))
            print(f"  匹配度: {match_score}")
    
    # 直接调用 keyword_search
    print("\n" + "=" * 70)
    print("调用 keyword_search 方法")
    print("=" * 70)
    
    search_result = search.keyword_search(test_query, threshold=0.5, top_k=3)
    print(f"\n搜索结果数量: {len(search_result)}")
    if search_result:
        print("✅ keyword_search 成功找到结果！")
        for r in search_result:
            print(f"\n  - {r.get('introduction_preview', '')[:50]}...")
            print(f"    匹配度: {r.get('match_score', 0):.2f}")
    else:
        print("❌ keyword_search 未找到结果")
    
    print("\n" + "=" * 70)
    print("诊断完成")
    print("=" * 70)

if __name__ == '__main__':
    diagnose_lancedb()