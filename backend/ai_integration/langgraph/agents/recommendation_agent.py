from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.tools import Tool
from ..tools.time_tool import get_time
from ..tools import keyword_search_tool, semantic_search_tool, read_chunk_tool
from ..utils.prompt_manager import get_prompt
import os
import logging

logger = logging.getLogger(__name__)

def create_recommendation_agent():
    """创建推荐代理"""
    try:
        api_key = os.getenv('api_key')
        base_url = os.getenv('base_url')
        
        llm = ChatOpenAI(
            model="qwen-turbo-2025-07-15", 
            temperature=0.7,
            api_key=api_key,
            base_url=base_url
        )
        
        tools = [
            keyword_search_tool,
            semantic_search_tool,
            read_chunk_tool,
            get_time
        ]
        
        llm_with_tools = llm.bind_tools(tools)
        
        system_prompt = get_prompt('recommendation')
        
        prompt = ChatPromptTemplate.from_messages([
            ("system", system_prompt),
            ("user", "{messages}")
        ])
        
        agent_chain = prompt | llm_with_tools
        
        return agent_chain
        
    except Exception as e:
        logger.error(f"Error creating recommendation agent: {str(e)}")
        import traceback
        logger.error(traceback.format_exc())
        raise