#!/usr/bin/env python3
"""分析语义相似度差异的原因"""

import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'backend'))

# 直接使用 sentence_transformers 分析
from sentence_transformers import SentenceTransformer, util

def analyze_similarity():
    print("=" * 70)
    print("分析语义相似度差异原因")
    print("=" * 70)
    
    model = SentenceTransformer('all-MiniLM-L6-v2')
    
    # 测试词对
    test_pairs = [
        ("运维", "前端开发"),
        ("运维", "Python开发"),
        ("运维", "网络工程"),
        ("运维", "系统运维"),
        ("运维", "DevOps"),
        ("运维", "网络安全"),
        ("网络工程", "网络安全"),
        ("前端开发", "后端开发"),
        ("DevOps", "Python"),
        ("运维", "服务器维护")
    ]
    
    print("\n词语对相似度分析:")
    print("-" * 70)
    print(f"{'词语对':<20} {'余弦相似度':<15} {'相关性判断'}")
    print("-" * 70)
    
    for word1, word2 in test_pairs:
        emb1 = model.encode(word1, normalize_embeddings=True)
        emb2 = model.encode(word2, normalize_embeddings=True)
        similarity = util.cos_sim(emb1, emb2).item()
        
        # 判断相关性
        if similarity > 0.5:
            relation = "强相关"
        elif similarity > 0.3:
            relation = "中等相关"
        else:
            relation = "弱相关"
        
        print(f"{word1} - {word2:<10} {similarity:<15.4f} {relation}")
    
    # 分析实际记录内容
    print("\n" + "=" * 70)
    print("实际记录文本分析")
    print("=" * 70)
    
    records = [
        {"name": "前端开发", "text": "热爱前端开发，熟练掌握HTML、CSS、JavaScript，熟悉React和Vue框架"},
        {"name": "Python开发", "text": "擅长Python开发和数据分析，熟悉Pandas、NumPy等数据处理库"},
        {"name": "网络工程", "text": "对网络工程有浓厚兴趣，熟悉网络协议、配置和维护，了解TCP/IP协议栈"},
        {"name": "网络安全", "text": "对网络安全有浓厚兴趣，熟悉渗透测试、漏洞分析，掌握Kali Linux工具"}
    ]
    
    query = "运维"
    query_emb = model.encode(query, normalize_embeddings=True)
    
    print(f"\n查询词: '{query}'")
    print("-" * 70)
    
    for record in records:
        record_emb = model.encode(record["text"], normalize_embeddings=True)
        similarity = util.cos_sim(query_emb, record_emb).item()
        
        # 分析关键词重叠
        query_tokens = set(query.lower())
        text_tokens = set(record["text"].lower())
        overlap = query_tokens.intersection(text_tokens)
        
        print(f"\n{record['name']}:")
        print(f"  相似度: {similarity:.4f}")
        print(f"  文本长度: {len(record['text'])} 字符")
        print(f"  字符重叠: {overlap}")
        print(f"  文本预览: {record['text'][:30]}...")

if __name__ == '__main__':
    analyze_similarity()