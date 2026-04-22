#!/usr/bin/env python3
"""查看 LanceDB 中所有数据与查询词的余弦相似度"""

import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'backend'))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.arag_integration import ARAGCareerSearch

def show_cosine_similarity(query="运维"):
    print("=" * 70)
    print(f"计算所有数据与 '{query}' 的余弦相似度")
    print("=" * 70)
    
    search = ARAGCareerSearch()
    
    # 获取所有记录
    results = search.table.to_pandas().to_dict('records')
    print(f"\n数据库中共有 {len(results)} 条记录")
    
    # 加载模型
    model = search._get_model()
    if model is None:
        print("❌ 无法加载语义模型，将使用关键词匹配")
        return
    
    print("✅ 语义模型加载成功")
    
    # 生成查询向量
    query_embedding = model.encode([query], normalize_embeddings=True)[0]
    
    # 计算相似度
    similarities = []
    for idx, record in enumerate(results):
        content = record.get("introduction_preview", record.get("self_introduction", ""))
        record_id = record.get("id", f"record_{idx}")
        
        # 生成记录向量
        record_embedding = model.encode([content], normalize_embeddings=True)[0]
        
        # 计算余弦相似度
        import numpy as np
        similarity = np.dot(query_embedding, record_embedding)
        
        # 获取推荐职业信息
        import json
        recommendations = []
        try:
            recs = json.loads(record.get("recommendations", "[]"))
            recommendations = [r.get("career", "") for r in recs[:3]]
        except:
            pass
        
        similarities.append({
            "id": record_id,
            "preview": content[:50] + "..." if len(content) > 50 else content,
            "similarity": similarity,
            "recommendations": recommendations
        })
    
    # 按相似度排序
    similarities.sort(key=lambda x: x["similarity"], reverse=True)
    
    # 显示结果
    print("\n" + "=" * 70)
    print("相似度排序结果")
    print("=" * 70)
    print(f"{'排名':<4} {'相似度':<10} {'记录预览'}")
    print("-" * 70)
    
    for i, item in enumerate(similarities, 1):
        print(f"{i:<4} {item['similarity']:<10.4f} {item['preview']}")
        if item['recommendations']:
            print(f"{'':<15} 推荐职业: {', '.join(item['recommendations'])}")
    
    # 统计信息
    avg_similarity = sum(s["similarity"] for s in similarities) / len(similarities)
    max_similarity = max(s["similarity"] for s in similarities)
    min_similarity = min(s["similarity"] for s in similarities)
    
    print("\n" + "=" * 70)
    print("统计信息")
    print("=" * 70)
    print(f"平均相似度: {avg_similarity:.4f}")
    print(f"最高相似度: {max_similarity:.4f}")
    print(f"最低相似度: {min_similarity:.4f}")
    
    # 阈值分析
    thresholds = [0.3, 0.4, 0.5, 0.6, 0.7, 0.8]
    print("\n不同阈值下的匹配数量:")
    for threshold in thresholds:
        count = sum(1 for s in similarities if s["similarity"] >= threshold)
        print(f"  阈值 {threshold}: {count} 条匹配")

if __name__ == '__main__':
    # 允许通过命令行参数指定查询词
    query = sys.argv[1] if len(sys.argv) > 1 else "运维"
    show_cosine_similarity(query)