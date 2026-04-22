#!/usr/bin/env python3
"""
调试 LanceDB 数据
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.arag_integration import ARAGCareerSearch

def debug_lancedb():
    """调试 LanceDB 数据"""
    print("\n" + "="*60)
    print("🚀 调试 LanceDB 数据")
    print("="*60)
    
    # 创建搜索器实例
    search = ARAGCareerSearch()
    
    # 获取所有数据
    print("\n📌 检查 LanceDB 表数据")
    print("-" * 40)
    
    try:
        # 获取所有记录
        results = search.table.to_pandas().to_dict('records')
        print(f"总记录数: {len(results)}")
        
        if len(results) == 0:
            print("❌ LanceDB 中没有数据！")
            print("\n📌 检查 MySQL 数据库中的 CareerRecommendationRecord")
            from ai_integration.models import CareerRecommendationRecord
            db_records = CareerRecommendationRecord.objects.all()
            print(f"MySQL 中的记录数: {db_records.count()}")
            
            if db_records.count() > 0:
                print("\n📌 需要同步数据到 LanceDB")
                print("运行以下命令同步数据:")
                print("python manage.py shell")
                print(">>> from ai_integration.langgraph_views import add_to_lancedb")
                print(">>> from ai_integration.models import CareerRecommendationRecord")
                print(">>> for record in CareerRecommendationRecord.objects.all():")
                print(">>>>     add_to_lancedb(record)")
            return
        
        # 打印前3条记录
        print("\n📋 前3条记录内容:")
        for i, record in enumerate(results[:3]):
            print(f"\n记录 {i+1}:")
            print(f"  ID: {record.get('id')}")
            print(f"  user_id: {record.get('user_id')}")
            
            # 检查 self_introduction
            intro = record.get('self_introduction', '')
            print(f"  self_introduction (长度): {len(intro)}")
            if len(intro) > 0:
                print(f"  self_introduction (前100字符): {intro[:100]}...")
            
            # 检查 introduction_preview
            preview = record.get('introduction_preview', '')
            print(f"  introduction_preview (长度): {len(preview)}")
            if len(preview) > 0:
                print(f"  introduction_preview (前100字符): {preview[:100]}...")
            
            # 检查 recommendations
            recs = record.get('recommendations', '')
            print(f"  recommendations (长度): {len(str(recs))}")
        
        # 测试分词
        print("\n" + "="*60)
        print("📌 测试分词功能")
        print("="*60)
        
        test_queries = ["go Java python", "Python Django", "Java"]
        for query in test_queries:
            tokens = search._tokenize_chinese(query)
            print(f"查询: '{query}' -> 分词结果: {tokens}")
        
        # 测试搜索
        print("\n" + "="*60)
        print("📌 测试搜索逻辑")
        print("="*60)
        
        query = "go Java python"
        query_words = set(search._tokenize_chinese(query))
        print(f"查询词分词: {query_words}")
        
        for record in results[:3]:
            content = record.get("introduction_preview", record.get("self_introduction", ""))
            if not isinstance(content, str):
                content = str(content) if content else ""
            record_words = set(search._tokenize_chinese(content))
            common_words = query_words.intersection(record_words)
            print(f"\n记录 {record.get('id')}:")
            print(f"  内容分词数: {len(record_words)}")
            print(f"  匹配词: {common_words}")
            if common_words:
                match_score = len(common_words) / len(query_words)
                print(f"  匹配度: {match_score:.4f}")
    
    except Exception as e:
        print(f"❌ 错误: {str(e)}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    debug_lancedb()