#!/usr/bin/env python3
"""
A-RAG 搜索工具模块

提供关键词搜索、语义搜索和块读取功能，可作为独立工具供其他 agent 调用。
"""

import os
import sys
import json
import logging
from typing import List, Dict, Any, Optional

# 设置日志
logger = logging.getLogger(__name__)

# 添加项目路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

# Django 设置
import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.arag_integration import ARAGCareerSearch

# 全局搜索器实例（懒加载）
_search_instance = None

def get_search_instance() -> ARAGCareerSearch:
    """获取搜索器实例（单例模式）"""
    global _search_instance
    if _search_instance is None:
        _search_instance = ARAGCareerSearch()
    return _search_instance

def keyword_search(
    query: str,
    threshold: float = 0.7,
    top_k: int = 5
) -> Dict[str, Any]:
    """
    关键词搜索工具
    
    Args:
        query: 搜索查询字符串
        threshold: 匹配度阈值（0-1）
        top_k: 返回的最大结果数
        
    Returns:
        包含搜索结果的字典
    """
    try:
        search = get_search_instance()
        results = search.keyword_search(query, threshold=threshold, top_k=top_k)
        
        return {
            "success": True,
            "tool_name": "keyword_search",
            "query": query,
            "count": len(results),
            "results": [
                {
                    "id": r.get("id"),
                    "preview": r.get("introduction_preview", r.get("self_introduction", ""))[:100],
                    "match_score": r.get("match_score", 0),
                    "match_type": r.get("match_type", "keyword"),
                    "recommendations": json.loads(r.get("recommendations", "[]"))[:3] if r.get("recommendations") else []
                }
                for r in results
            ]
        }
    except Exception as e:
        logger.error(f"Keyword search error: {str(e)}")
        return {
            "success": False,
            "tool_name": "keyword_search",
            "query": query,
            "error": str(e),
            "results": []
        }

def semantic_search(
    query: str,
    threshold: float = 0.5,
    top_k: int = 5
) -> Dict[str, Any]:
    """
    语义搜索工具
    
    Args:
        query: 搜索查询字符串
        threshold: 相似度阈值（0-1）
        top_k: 返回的最大结果数
        
    Returns:
        包含搜索结果的字典
    """
    try:
        search = get_search_instance()
        results = search.semantic_search(query, threshold=threshold, top_k=top_k)
        
        return {
            "success": True,
            "tool_name": "semantic_search",
            "query": query,
            "count": len(results),
            "results": [
                {
                    "id": r.get("id"),
                    "preview": r.get("introduction_preview", r.get("self_introduction", ""))[:100],
                    "match_score": r.get("match_score", 0),
                    "match_type": r.get("match_type", "semantic"),
                    "recommendations": json.loads(r.get("recommendations", "[]"))[:3] if r.get("recommendations") else []
                }
                for r in results
            ]
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

def hybrid_search(
    query: str,
    keyword_threshold: float = 0.5,
    semantic_threshold: float = 0.5,
    top_k: int = 5
) -> Dict[str, Any]:
    """
    混合搜索工具（结合关键词和语义搜索）
    
    Args:
        query: 搜索查询字符串
        keyword_threshold: 关键词匹配度阈值
        semantic_threshold: 语义相似度阈值
        top_k: 返回的最大结果数
        
    Returns:
        包含搜索结果的字典
    """
    try:
        search = get_search_instance()
        results = search.hybrid_search(
            query,
            keyword_threshold=keyword_threshold,
            semantic_threshold=semantic_threshold,
            top_k=top_k
        )
        
        return {
            "success": True,
            "tool_name": "hybrid_search",
            "query": query,
            "count": len(results),
            "results": [
                {
                    "id": r.get("id"),
                    "preview": r.get("introduction_preview", r.get("self_introduction", ""))[:100],
                    "match_score": r.get("match_score", 0),
                    "match_type": r.get("match_type", "hybrid"),
                    "recommendations": json.loads(r.get("recommendations", "[]"))[:3] if r.get("recommendations") else []
                }
                for r in results
            ]
        }
    except Exception as e:
        logger.error(f"Hybrid search error: {str(e)}")
        return {
            "success": False,
            "tool_name": "hybrid_search",
            "query": query,
            "error": str(e),
            "results": []
        }

def read_chunk(chunk_id: str) -> Dict[str, Any]:
    """
    块读取工具 - 根据ID读取完整记录
    
    Args:
        chunk_id: 记录ID
        
    Returns:
        包含完整记录的字典
    """
    try:
        search = get_search_instance()
        result = search.read_full_block(chunk_id)
        
        if result.get("success") and result.get("record"):
            record = result["record"]
            return {
                "success": True,
                "tool_name": "read_chunk",
                "chunk_id": chunk_id,
                "record": {
                    "id": record.get("id"),
                    "user_id": record.get("user_id"),
                    "self_introduction": record.get("self_introduction", ""),
                    "introduction_preview": record.get("introduction_preview", ""),
                    "recommendations": json.loads(record.get("recommendations", "[]")) if record.get("recommendations") else [],
                    "analysis_result": record.get("analysis_result", ""),
                    "created_at": record.get("created_at", "")
                }
            }
        else:
            return {
                "success": False,
                "tool_name": "read_chunk",
                "chunk_id": chunk_id,
                "error": "Record not found or already read"
            }
    except Exception as e:
        logger.error(f"Read chunk error: {str(e)}")
        return {
            "success": False,
            "tool_name": "read_chunk",
            "chunk_id": chunk_id,
            "error": str(e)
        }

def add_record(record: Dict[str, Any]) -> Dict[str, Any]:
    """
    添加记录工具
    
    Args:
        record: 要添加的记录字典，包含以下字段：
            - id: 记录ID（可选，自动生成）
            - user_id: 用户ID
            - self_introduction: 自我介绍
            - introduction_preview: 预览摘要
            - recommendations: 推荐职业列表（JSON字符串或列表）
            - analysis_result: 分析结果
            
    Returns:
        添加结果
    """
    try:
        search = get_search_instance()
        
        # 处理 recommendations 字段
        if isinstance(record.get("recommendations"), list):
            record["recommendations"] = json.dumps(record["recommendations"])
        
        search.add_record(record)
        
        return {
            "success": True,
            "tool_name": "add_record",
            "record_id": record.get("id", "auto-generated"),
            "message": "Record added successfully"
        }
    except Exception as e:
        logger.error(f"Add record error: {str(e)}")
        return {
            "success": False,
            "tool_name": "add_record",
            "error": str(e)
        }

def get_record_count() -> Dict[str, Any]:
    """
    获取记录总数工具
    
    Returns:
        记录总数
    """
    try:
        search = get_search_instance()
        df = search.table.to_pandas()
        return {
            "success": True,
            "tool_name": "get_record_count",
            "count": len(df)
        }
    except Exception as e:
        logger.error(f"Get record count error: {str(e)}")
        return {
            "success": False,
            "tool_name": "get_record_count",
            "error": str(e)
        }

# 工具元数据（用于 LangChain 等框架集成）
TOOL_METADATA = {
    "keyword_search": {
        "name": "keyword_search",
        "description": "根据关键词搜索职业推荐记录",
        "parameters": {
            "query": {"type": "string", "description": "搜索查询词", "required": True},
            "threshold": {"type": "number", "description": "匹配度阈值（0-1）", "default": 0.7},
            "top_k": {"type": "integer", "description": "返回最大结果数", "default": 5}
        }
    },
    "semantic_search": {
        "name": "semantic_search",
        "description": "根据语义相似度搜索职业推荐记录",
        "parameters": {
            "query": {"type": "string", "description": "搜索查询词", "required": True},
            "threshold": {"type": "number", "description": "相似度阈值（0-1）", "default": 0.5},
            "top_k": {"type": "integer", "description": "返回最大结果数", "default": 5}
        }
    },
    "hybrid_search": {
        "name": "hybrid_search",
        "description": "结合关键词和语义搜索职业推荐记录",
        "parameters": {
            "query": {"type": "string", "description": "搜索查询词", "required": True},
            "keyword_threshold": {"type": "number", "description": "关键词匹配阈值", "default": 0.5},
            "semantic_threshold": {"type": "number", "description": "语义相似度阈值", "default": 0.5},
            "top_k": {"type": "integer", "description": "返回最大结果数", "default": 5}
        }
    },
    "read_chunk": {
        "name": "read_chunk",
        "description": "根据ID读取完整的职业推荐记录",
        "parameters": {
            "chunk_id": {"type": "string", "description": "记录ID", "required": True}
        }
    },
    "add_record": {
        "name": "add_record",
        "description": "添加新的职业推荐记录",
        "parameters": {
            "id": {"type": "string", "description": "记录ID（可选）"},
            "user_id": {"type": "string", "description": "用户ID"},
            "self_introduction": {"type": "string", "description": "自我介绍", "required": True},
            "introduction_preview": {"type": "string", "description": "预览摘要"},
            "recommendations": {"type": "string", "description": "推荐职业（JSON字符串）"},
            "analysis_result": {"type": "string", "description": "分析结果"}
        }
    },
    "get_record_count": {
        "name": "get_record_count",
        "description": "获取数据库中的记录总数"
    }
}

# 工具列表（用于动态调用）
TOOLS = {
    "keyword_search": keyword_search,
    "semantic_search": semantic_search,
    "hybrid_search": hybrid_search,
    "read_chunk": read_chunk,
    "add_record": add_record,
    "get_record_count": get_record_count
}

def call_tool(tool_name: str, **kwargs) -> Dict[str, Any]:
    """
    统一调用工具的入口函数
    
    Args:
        tool_name: 工具名称
        **kwargs: 工具参数
        
    Returns:
        工具执行结果
    """
    if tool_name not in TOOLS:
        return {
            "success": False,
            "tool_name": tool_name,
            "error": f"Tool '{tool_name}' not found"
        }
    
    try:
        return TOOLS[tool_name](**kwargs)
    except Exception as e:
        return {
            "success": False,
            "tool_name": tool_name,
            "error": str(e)
        }

if __name__ == '__main__':
    # 测试工具
    print("=" * 60)
    print("测试 A-RAG 搜索工具")
    print("=" * 60)
    
    # 测试关键词搜索
    print("\n1. 测试 keyword_search")
    result = keyword_search("Python 数据分析")
    print(f"   成功: {result['success']}")
    print(f"   结果数: {result['count']}")
    
    # 测试语义搜索
    print("\n2. 测试 semantic_search")
    result = semantic_search("网络安全")
    print(f"   成功: {result['success']}")
    print(f"   结果数: {result['count']}")
    
    # 测试记录总数
    print("\n3. 测试 get_record_count")
    result = get_record_count()
    print(f"   成功: {result['success']}")
    print(f"   记录数: {result['count']}")
    
    print("\n" + "=" * 60)
    print("测试完成！")
    print("=" * 60)