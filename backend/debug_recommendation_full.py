#!/usr/bin/env python3
"""
推荐代理完整调试脚本
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

def test_recommendation_agent():
    """测试推荐代理"""
    print("\n" + "="*70)
    print("测试推荐代理")
    print("="*70)
    
    from ai_integration.langgraph.agents.recommendation_agent import create_recommendation_agent
    from langchain_core.messages import HumanMessage
    
    agent = create_recommendation_agent()
    print("✅ 代理创建成功")
    
    # 测试消息
    messages = [
        HumanMessage(content="我精通Python编程，熟悉Django框架，有2年后端开发经验，参与过多个大型项目。请推荐适合我的职业。")
    ]
    
    print("\n🔄 调用代理...")
    result = agent.invoke({"messages": messages})
    
    print(f"\n📊 代理返回类型: {type(result).__name__}")
    print(f"📋 返回结果:")
    
    # 检查是否是字典
    if isinstance(result, dict):
        print(f"   是字典类型")
        print(f"   键: {list(result.keys())}")
        
        # 检查 messages
        if 'messages' in result:
            messages = result['messages']
            last_message = messages[-1] if messages else None
            
            if last_message:
                print(f"\n   最后一条消息类型: {type(last_message).__name__}")
                
                # 检查工具调用
                if hasattr(last_message, 'tool_calls'):
                    tool_calls = last_message.tool_calls
                    print(f"   ✅ 检测到工具调用! 数量: {len(tool_calls)}")
                    for i, tool_call in enumerate(tool_calls):
                        print(f"\n      工具调用 {i+1}:")
                        if isinstance(tool_call, dict):
                            print(f"         名称: {tool_call.get('name')}")
                            print(f"         参数: {json.dumps(tool_call.get('args', {}), indent=4, ensure_ascii=False)}")
                        else:
                            print(f"         名称: {tool_call.name}")
                            print(f"         参数: {json.dumps(tool_call.args, indent=4, ensure_ascii=False)}")
                else:
                    print(f"   ❌ 未检测到工具调用")
                    if hasattr(last_message, 'content'):
                        print(f"   响应内容预览: {last_message.content[:200]}...")
        else:
            print(f"   没有 messages 字段")
    
    else:
        print(f"   不是字典类型")
        print(f"   内容: {str(result)[:500]}...")
    
    return result

def test_graph_execution():
    """测试完整的graph执行"""
    print("\n" + "="*70)
    print("测试完整Graph执行")
    print("="*70)
    
    from ai_integration.langgraph.graphs.graph import build_graph
    
    try:
        graph = build_graph()
        print("✅ Graph构建成功")
        print(f"   Graph类型: {type(graph).__name__}")
        
        # 检查节点
        if hasattr(graph, 'nodes'):
            print(f"   节点数量: {len(graph.nodes)}")
            for node_name in graph.nodes:
                print(f"   - {node_name}")
        
        # 检查边
        if hasattr(graph, 'edges'):
            print(f"   边数量: {len(graph.edges)}")
        
    except Exception as e:
        print(f"❌ Graph构建失败: {str(e)}")
        import traceback
        traceback.print_exc()

def test_tool_direct_call():
    """测试工具直接调用"""
    print("\n" + "="*70)
    print("测试工具直接调用")
    print("="*70)
    
    from ai_integration.langgraph.tools import keyword_search_tool, semantic_search_tool
    
    # 测试关键词搜索
    print("\n🔍 测试关键词搜索:")
    result = keyword_search_tool.invoke({"query": "Python开发", "threshold": 0.7, "top_k": 3})
    print(f"✅ 执行成功")
    print(f"   结果数量: {result.get('count', 0)}")
    if result.get('results'):
        for r in result['results'][:2]:
            print(f"   - {r.get('introduction_preview', '')[:30]}...")
    
    # 测试语义搜索
    print("\n🔍 测试语义搜索:")
    result = semantic_search_tool.invoke({"query": "Python开发", "threshold": 0.5, "top_k": 3})
    print(f"✅ 执行成功")
    print(f"   结果数量: {result.get('count', 0)}")
    if result.get('results'):
        for r in result['results'][:2]:
            print(f"   - {r.get('introduction_preview', '')[:30]}...")

def main():
    print("="*70)
    print("推荐代理完整调试")
    print("="*70)
    
    test_recommendation_agent()
    test_graph_execution()
    test_tool_direct_call()
    
    print("\n" + "="*70)
    print("调试完成!")
    print("="*70)

if __name__ == "__main__":
    main()