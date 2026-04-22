from langchain_core.tools import tool
from .time_tool import get_time

try:
    from .keyword_search import keyword_search
    from .semantic_search import semantic_search
    from .read_chunk import read_chunk

    @tool
    def keyword_search_tool(query: str, threshold: float = 0.7, top_k: int = 5) -> dict:
        """
        根据关键词搜索职业推荐记录
        
        Args:
            query: 搜索查询词
            threshold: 匹配度阈值（0-1）
            top_k: 返回的最大结果数
        
        Returns:
            包含搜索结果的字典
        """
        return keyword_search(query, threshold, top_k)

    @tool
    def semantic_search_tool(query: str, threshold: float = 0.5, top_k: int = 5) -> dict:
        """
        根据语义相似度搜索职业推荐记录
        
        Args:
            query: 搜索查询词
            threshold: 相似度阈值（0-1）
            top_k: 返回的最大结果数
        
        Returns:
            包含搜索结果的字典
        """
        return semantic_search(query, threshold, top_k)

    @tool
    def read_chunk_tool(chunk_id: str) -> dict:
        """
        根据ID读取完整的职业推荐记录
        
        Args:
            chunk_id: 记录ID
        
        Returns:
            包含完整记录的字典
        """
        return read_chunk(chunk_id)

    __all__ = ["get_time", "keyword_search_tool", "semantic_search_tool", "read_chunk_tool"]
    
except Exception as e:
    import logging
    logger = logging.getLogger(__name__)
    logger.error(f"Failed to import search tools: {str(e)}")
    __all__ = ["get_time"]
