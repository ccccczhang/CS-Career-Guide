#!/usr/bin/env python3
"""
完整Graph流程调试脚本
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

def test_graph_with_recommendation():
    """测试推荐模式的完整Graph流程"""
    print("\n" + "="*70)
    print("测试推荐模式的完整Graph流程")
    print("="*70)
    
    # 修复导入路径问题
    original_path = sys.path.copy()
    try:
        sys.path = [p for p in sys.path if not (p and 'ai_integration/langgraph' in p.replace('/', os.sep))]
        
        from langgraph import StateGraph, START, END
        
        # 恢复路径
        sys.path = original_path
        
        from ai_integration.langgraph.graphs.graph import create_chat_workflow
        from langchain_core.messages import HumanMessage
        
        print("✅ 导入成功")
        
        # 创建工作流
        graph = create_chat_workflow()
        print("✅ Graph创建成功")
        
        # 构建输入
        messages = [
            HumanMessage(content="我精通Python编程，熟悉Django框架，有2年后端开发经验。")
        ]
        
        state = {
            "messages": messages,
            "mode": "recommendation",
            "response": "",
            "tool_calls": None,
            "tool_results": None,
            "last_agent": None
        }
        
        print("\n🔄 执行Graph...")
        result = graph.invoke(state)
        
        print(f"\n📊 结果类型: {type(result).__name__}")
        print(f"📋 结果键: {list(result.keys())}")
        
        # 检查结果
        if 'response' in result and result['response']:
            print(f"\n💬 响应内容: {result['response'][:200]}...")
        elif 'tool_calls' in result and result['tool_calls']:
            print(f"\n🛠️ 工具调用:")
            for tool_call in result['tool_calls']:
                print(f"   - 名称: {tool_call.get('name')}")
                print(f"     参数: {json.dumps(tool_call.get('args', {}), ensure_ascii=False)}")
        
        return result
        
    except Exception as e:
        sys.path = original_path
        print(f"❌ 测试失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return None

def test_agent_directly():
    """直接测试推荐代理"""
    print("\n" + "="*70)
    print("直接测试推荐代理")
    print("="*70)
    
    from ai_integration.langgraph.agents.recommendation_agent import create_recommendation_agent
    from langchain_core.messages import HumanMessage
    
    agent = create_recommendation_agent()
    print("✅ 代理创建成功")
    
    # 测试消息 - 使用更明确的指令
    messages = [
        HumanMessage(content="我精通Python编程，熟悉Django框架，有2年后端开发经验。请使用工具搜索适合我的职业推荐。")
    ]
    
    print("\n🔄 调用代理...")
    result = agent.invoke({"messages": messages})
    
    print(f"\n📊 返回类型: {type(result).__name__}")
    
    # 检查工具调用
    if hasattr(result, 'tool_calls') and result.tool_calls:
        print(f"✅ 检测到工具调用!")
        for tool_call in result.tool_calls:
            if isinstance(tool_call, dict):
                print(f"   工具名称: {tool_call.get('name')}")
                print(f"   参数: {json.dumps(tool_call.get('args', {}), indent=2, ensure_ascii=False)}")
            else:
                print(f"   工具名称: {tool_call.name}")
                print(f"   参数: {json.dumps(tool_call.args, indent=2, ensure_ascii=False)}")
    else:
        print(f"❌ 未检测到工具调用")
        if hasattr(result, 'content'):
            print(f"   响应内容: {result.content[:300]}...")
    
    return result

def check_and_fix_prompt():
    """检查并修复提示词"""
    print("\n" + "="*70)
    print("检查并修复提示词")
    print("="*70)
    
    from ai_integration.langgraph.utils.prompt_manager import get_prompt
    
    prompt = get_prompt('recommendation')
    
    # 检查是否包含工具调用指令
    tool_indicators = [
        'keyword_search_tool',
        'semantic_search_tool', 
        'read_chunk_tool',
        '必须调用',
        '使用工具',
        '搜索'
    ]
    
    print("📝 检查提示词内容:")
    for indicator in tool_indicators:
        found = indicator in prompt
        print(f"   {'✅' if found else '❌'} '{indicator}'")
    
    # 打印提示词预览
    print(f"\n📋 提示词预览（前500字符）:")
    print("-" * 50)
    print(prompt[:500])
    print("-" * 50)
    
    # 如果缺少工具调用指令，添加
    if not all(indicator in prompt for indicator in ['keyword_search_tool', '必须调用']):
        print("\n🔧 需要更新提示词，添加工具调用指令")
        return False
    else:
        print("\n✅ 提示词包含工具调用指令")
        return True

def main():
    print("="*70)
    print("Graph流程完整调试")
    print("="*70)
    
    check_and_fix_prompt()
    test_agent_directly()
    
    print("\n" + "="*70)
    print("调试完成!")
    print("="*70)

if __name__ == "__main__":
    main()