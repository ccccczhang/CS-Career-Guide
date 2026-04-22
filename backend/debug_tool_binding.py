#!/usr/bin/env python3
"""
工具绑定调试脚本

检查推荐代理的工具绑定情况和graph流程
"""

import os
import sys
import json
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

def check_tool_registration():
    """检查工具注册情况"""
    print("\n" + "="*70)
    print("1. 检查工具注册")
    print("="*70)
    
    from ai_integration.langgraph.tools import (
        keyword_search_tool,
        semantic_search_tool,
        read_chunk_tool
    )
    
    tools = [keyword_search_tool, semantic_search_tool, read_chunk_tool]
    
    for tool in tools:
        print(f"\n✅ 工具: {tool.name}")
        print(f"   类型: {type(tool).__name__}")
        print(f"   描述: {tool.description}")
        print(f"   参数: {json.dumps(tool.args, indent=2, ensure_ascii=False)}")
        
        # 检查是否有 tool_spec
        if hasattr(tool, 'tool_spec'):
            print(f"   tool_spec: {tool.tool_spec}")
        
        # 检查是否有 name 属性（langchain tool 需要）
        if hasattr(tool, 'name'):
            print(f"   name: {tool.name}")
    
    return tools

def check_agent_tool_binding():
    """检查代理的工具绑定"""
    print("\n" + "="*70)
    print("2. 检查代理工具绑定")
    print("="*70)
    
    from ai_integration.langgraph.agents.recommendation_agent import create_recommendation_agent
    
    agent = create_recommendation_agent()
    print(f"✅ 代理创建成功")
    print(f"   代理类型: {type(agent).__name__}")
    
    # 检查绑定的工具
    try:
        # 获取绑定的工具信息
        if hasattr(agent, 'steps'):
            for i, step in enumerate(agent.steps):
                print(f"\n   Step {i}: {type(step).__name__}")
                if hasattr(step, 'kwargs'):
                    if 'tools' in step.kwargs:
                        tools = step.kwargs['tools']
                        print(f"      绑定的工具数量: {len(tools)}")
                        for tool in tools:
                            print(f"      - {tool.name}")
    
    except Exception as e:
        print(f"   无法获取绑定信息: {str(e)}")
    
    return agent

def test_tool_calling_format():
    """测试工具调用格式"""
    print("\n" + "="*70)
    print("3. 测试工具调用格式")
    print("="*70)
    
    from langchain_core.messages import HumanMessage, AIMessage
    from langchain_openai import ChatOpenAI
    from ai_integration.langgraph.tools import keyword_search_tool, semantic_search_tool
    
    llm = ChatOpenAI(
        model="qwen-turbo-2025-07-15",
        temperature=0.7,
        api_key=os.getenv('api_key'),
        base_url=os.getenv('base_url')
    )
    
    tools = [keyword_search_tool, semantic_search_tool]
    llm_with_tools = llm.bind_tools(tools)
    
    # 测试提示词
    prompt = """你是一个职业推荐助手。当用户询问职业推荐时，你必须使用 keyword_search_tool 搜索相关信息。
    
    现在用户说：请帮我搜索Python开发相关的职业推荐。
    
    你必须调用 keyword_search_tool，参数：query="Python开发", threshold=0.7, top_k=3
    
    输出格式：直接调用工具，不要添加其他内容。
    """
    
    message = HumanMessage(content=prompt)
    result = llm_with_tools.invoke([message])
    
    print(f"📊 响应类型: {type(result).__name__}")
    
    if hasattr(result, 'tool_calls') and result.tool_calls:
        print(f"✅ 检测到工具调用!")
        for tool_call in result.tool_calls:
            # 处理不同格式的工具调用
            if isinstance(tool_call, dict):
                print(f"   工具名称: {tool_call.get('name', '未知')}")
                print(f"   参数: {json.dumps(tool_call.get('args', {}), indent=2, ensure_ascii=False)}")
            else:
                print(f"   工具名称: {tool_call.name if hasattr(tool_call, 'name') else '未知'}")
                print(f"   参数: {json.dumps(tool_call.args if hasattr(tool_call, 'args') else {}, indent=2, ensure_ascii=False)}")
    else:
        print(f"❌ 未检测到工具调用")
        print(f"   响应内容: {result.content[:200] if hasattr(result, 'content') else str(result)}")
    
    return result

def check_prompt_content():
    """检查提示词内容"""
    print("\n" + "="*70)
    print("4. 检查提示词内容")
    print("="*70)
    
    from ai_integration.langgraph.utils.prompt_manager import get_prompt
    
    prompt = get_prompt('recommendation')
    print(f"📝 提示词长度: {len(prompt)} 字符")
    print(f"📋 提示词内容预览（前500字符）:")
    print("-" * 50)
    print(prompt[:500])
    print("-" * 50)
    
    # 检查是否包含工具相关的内容
    tool_indicators = ['keyword_search_tool', 'semantic_search_tool', 'read_chunk_tool', '工具调用', '必须调用']
    print("\n🔍 检查提示词中的工具相关内容:")
    for indicator in tool_indicators:
        found = indicator in prompt
        print(f"   {'✅' if found else '❌'} '{indicator}'")
    
    return prompt

def main():
    print("="*70)
    print("工具绑定调试")
    print("="*70)
    
    check_tool_registration()
    check_agent_tool_binding()
    test_tool_calling_format()
    check_prompt_content()
    
    print("\n" + "="*70)
    print("调试完成!")
    print("="*70)

if __name__ == "__main__":
    main()