#!/usr/bin/env python3
"""
块读取工具

根据ID读取完整的职业推荐记录
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

def read_chunk(chunk_id: str) -> dict:
    """
    块读取工具 - 根据ID读取完整记录
    
    Args:
        chunk_id: 记录ID
        
    Returns:
        包含完整记录的字典
    """
    import json
    
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

# 工具元数据
TOOL_METADATA = {
    "name": "read_chunk",
    "description": "根据ID读取完整的职业推荐记录",
    "parameters": {
        "chunk_id": {"type": "string", "description": "记录ID", "required": True}
    }
}

if __name__ == "__main__":
    # 测试（需要先执行搜索获取ID）
    from keyword_search import keyword_search
    
    search_result = keyword_search("Python", top_k=1)
    if search_result["count"] > 0:
        chunk_id = search_result["results"][0]["id"]
        result = read_chunk(chunk_id)
        print(f"读取记录成功: {result['success']}")
        if result["success"]:
            print(f"  预览: {result['record']['introduction_preview']}")