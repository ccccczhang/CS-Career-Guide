#!/usr/bin/env python3
"""诊断语义搜索的相似度计算"""

import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'backend'))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.arag_integration import ARAGCareerSearch

def test_semantic_similarity():
    print("=" * 70)
    print("诊断语义搜索相似度")
    print("=" * 70)
    
    search = ARAGCareerSearch()
    
    # 测试查询
    test_queries = [
        "网络安全",
        "我想从事网络安全相关工作",
        "网页开发",
        "前端开发"
    ]
    
    # 获取所有记录
    results = search.table.to_pandas().to_dict('records')
    print(f"\n数据库中有 {len(results)} 条记录")
    
    # 加载模型
    model = search._get_model()
    if model is None:
        print("❌ 无法加载模型")
        return
    
    print("✅ 模型加载成功")
    
    # 测试每个查询
    for query in test_queries:
        print(f"\n" + "=" * 50)
        print(f"查询: '{query}'")
        print("=" * 50)
        
        # 生成查询向量
        query_embedding = model.encode([query], normalize_embeddings=True)[0]
        
        # 计算与每条记录的相似度
        similarities = []
        for record in results:
            content = record.get("introduction_preview", record.get("self_introduction", ""))
            record_embedding = model.encode([content], normalize_embeddings=True)[0]
            
            # 计算余弦相似度
            import numpy as np
            similarity = np.dot(query_embedding, record_embedding)
            
            similarities.append({
                "preview": content[:30] + "..." if len(content) > 30 else content,
                "similarity": similarity
            })
        
        # 按相似度排序
        similarities.sort(key=lambda x: x["similarity"], reverse=True)
        
        # 显示结果
        print("\n相似度排序:")
        for i, item in enumerate(similarities, 1):
            print(f"{i}. 相似度: {item['similarity']:.4f} - {item['preview']}")
        
        # 显示阈值判断
        print(f"\n当前阈值: 0.8")
        matches = [s for s in similarities if s["similarity"] >= 0.8]
        print(f"阈值0.8时匹配数: {len(matches)}")
        
        matches_low = [s for s in similarities if s["similarity"] >= 0.5]
        print(f"阈值0.5时匹配数: {len(matches_low)}")

if __name__ == '__main__':
    test_semantic_similarity()