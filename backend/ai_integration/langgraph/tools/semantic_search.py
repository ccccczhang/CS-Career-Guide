#!/usr/bin/env python3
"""
语义搜索工具

基于 Sentence-BERT 的语义相似度匹配
"""

import os
import sys
import logging

logger = logging.getLogger(__name__)

# 添加项目路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', '..'))

# Django 设置
import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.arag_integration import ARAGCareerSearch

# 全局搜索器实例
_search_instance = None

def get_search_instance() -> ARAGCareerSearch:
    """获取搜索器实例（单例模式）"""
    global _search_instance
    if _search_instance is None:
        _search_instance = ARAGCareerSearch()
    return _search_instance

def semantic_search(
    query: str,
    threshold: float = 0.5,
    top_k: int = 5
) -> dict:
    """
    语义搜索工具
    
    Args:
        query: 搜索查询字符串
        threshold: 相似度阈值（0-1）
        top_k: 返回的最大结果数
        
    Returns:
        包含搜索结果的字典
    """
    import json
    
    try:
            search = get_search_instance()
            results = search.semantic_search(query, threshold=threshold, top_k=top_k)
            
            formatted_results = []
            for r in results:
                # 处理introduction_preview可能是float类型的问题
                preview = r.get("introduction_preview", "")
                if not isinstance(preview, str):
                    preview = r.get("self_introduction", "")
                
                # 确保preview是字符串
                if not isinstance(preview, str):
                    preview = str(preview) if preview else ""
                
                recommendations = []
                try:
                    recs = r.get("recommendations", "")
                    if isinstance(recs, str) and recs:
                        recommendations = json.loads(recs)[:3]
                except:
                    recommendations = []
                
                formatted_results.append({
                    "id": r.get("id"),
                    "preview": preview[:100],
                    "match_score": r.get("match_score", 0),
                    "match_type": r.get("match_type", "semantic"),
                    "recommendations": recommendations
                })
            
            return {
                "success": True,
                "tool_name": "semantic_search",
                "query": query,
                "count": len(formatted_results),
                "results": formatted_results
            }
    except Exception as e:
        logger.error(f"Semantic search error: {str(e)}")
        return {
            "success": False,
            "tool_name": "semantic_search",
            "query": query,
            "error": str(e),
            "results": []
        }

# 工具元数据
TOOL_METADATA = {
    "name": "semantic_search",
    "description": "根据语义相似度搜索职业推荐记录",
    "parameters": {
        "query": {"type": "string", "description": "搜索查询词", "required": True},
        "threshold": {"type": "number", "description": "相似度阈值（0-1）", "default": 0.5},
        "top_k": {"type": "integer", "description": "返回最大结果数", "default": 5}
    }
}

if __name__ == "__main__":
    # 测试
    result = semantic_search("网络安全")
    print(f"语义搜索结果: {result['count']} 条")
    for r in result["results"]:
        print(f"  - {r['preview']} (相似度: {r['match_score']:.4f})")