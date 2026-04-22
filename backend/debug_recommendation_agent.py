#!/usr/bin/env python3
"""
职业推荐代理工具调用调试脚本

测试推荐代理如何调用工具（关键词搜索、语义搜索、块读取）
"""

import os
import sys
import json
import logging

# 设置日志级别
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)

# 添加项目路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

# Django 设置
import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

def test_agent_creation():
    """测试推荐代理创建"""
    print("\n" + "="*70)
    print("1. 测试推荐代理创建")
    print("="*70)
    
    try:
        from ai_integration.langgraph.agents.recommendation_agent import create_recommendation_agent
        
        agent = create_recommendation_agent()
        print(f"✅ 代理创建成功")
        print(f"   代理类型: {type(agent)}")
        
        return agent
    except Exception as e:
        print(f"❌ 代理创建失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return None

def test_tool_registration():
    """测试工具注册"""
    print("\n" + "="*70)
    print("2. 测试工具注册")
    print("="*70)
    
    try:
        from ai_integration.langgraph.tools import (
            keyword_search_tool, 
            semantic_search_tool, 
            read_chunk_tool
        )
        
        tools = [keyword_search_tool, semantic_search_tool, read_chunk_tool]
        
        for tool in tools:
            print(f"✅ 工具注册成功: {tool.name}")
            print(f"   描述: {tool.description}")
            print(f"   参数: {json.dumps(tool.args, indent=2, ensure_ascii=False)}")
            print()
        
        return tools
    except Exception as e:
        print(f"❌ 工具注册失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return None

def test_tool_direct_call():
    """测试直接调用工具"""
    print("\n" + "="*70)
    print("3. 测试直接调用工具")
    print("="*70)
    
    try:
        from ai_integration.langgraph.tools import (
            keyword_search_tool, 
            semantic_search_tool, 
            read_chunk_tool
        )
        
        # 测试关键词搜索
        print("\n📝 测试关键词搜索工具:")
        result = keyword_search_tool.invoke({"query": "Python 数据分析", "threshold": 0.7, "top_k": 3})
        print(f"   结果: {json.dumps(result, indent=2, ensure_ascii=False)}")
        
        # 测试语义搜索
        print("\n📝 测试语义搜索工具:")
        result = semantic_search_tool.invoke({"query": "网络安全", "threshold": 0.5, "top_k": 3})
        print(f"   结果: {json.dumps(result, indent=2, ensure_ascii=False)}")
        
    except Exception as e:
        print(f"❌ 工具调用失败: {str(e)}")
        import traceback
        traceback.print_exc()

def test_agent_with_tools():
    """测试代理调用工具"""
    print("\n" + "="*70)
    print("4. 测试代理调用工具")
    print("="*70)
    
    try:
        from ai_integration.langgraph.agents.recommendation_agent import create_recommendation_agent
        from langchain_core.messages import HumanMessage
        
        agent = create_recommendation_agent()
        
        # 构建测试消息，引导代理调用工具
        test_messages = [
            HumanMessage(content="请帮我搜索关于Python开发的职业推荐")
        ]
        
        print(f"📝 发送消息: {test_messages[0].content}")
        
        result = agent.invoke({"messages": test_messages})
        
        print(f"\n📊 代理返回结果:")
        print(f"   类型: {type(result)}")
        
        if hasattr(result, "tool_calls") and result.tool_calls:
            print(f"   ✅ 检测到工具调用!")
            print(f"   工具调用数量: {len(result.tool_calls)}")
            
            for i, tool_call in enumerate(result.tool_calls):
                print(f"\n   工具调用 {i+1}:")
                if isinstance(tool_call, dict):
                    print(f"      名称: {tool_call.get('name')}")
                    print(f"      参数: {json.dumps(tool_call.get('args', {}), indent=4, ensure_ascii=False)}")
                else:
                    print(f"      名称: {tool_call.name}")
                    print(f"      参数: {json.dumps(tool_call.args, indent=4, ensure_ascii=False)}")
        else:
            print(f"   ❌ 未检测到工具调用")
            print(f"   内容: {result.content if hasattr(result, 'content') else result}")
            
        return result
        
    except Exception as e:
        print(f"❌ 代理调用失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return None

def test_graph_workflow():
    """测试完整的LangGraph工作流"""
    print("\n" + "="*70)
    print("5. 测试LangGraph工作流")
    print("="*70)
    
    try:
        from ai_integration.langgraph.graphs.graph import create_chat_workflow
        from langchain_core.messages import HumanMessage
        
        # 创建工作流
        app = create_chat_workflow()
        print("✅ 工作流创建成功")
        
        # 测试推荐模式
        test_input = {
            "messages": [HumanMessage(content="请帮我搜索关于Python开发的职业推荐")],
            "mode": "recommendation"
        }
        
        print(f"\n📝 输入: {test_input}")
        print("\n🚀 执行工作流...")
        
        # 执行工作流
        result = app.invoke(test_input)
        
        print(f"\n📊 工作流执行结果:")
        print(f"   response: {result.get('response', '')[:200]}...")
        print(f"   tool_calls: {result.get('tool_calls')}")
        print(f"   last_agent: {result.get('last_agent')}")
        
        return result
        
    except Exception as e:
        print(f"❌ 工作流执行失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return None

def test_full_recommendation_flow():
    """测试完整的职业推荐流程"""
    print("\n" + "="*70)
    print("6. 测试完整职业推荐流程")
    print("="*70)
    
    try:
        from ai_integration.optimized_views import optimized_career_recommendation
        from django.test import RequestFactory
        
        # 创建模拟请求
        factory = RequestFactory()
        request = factory.post('/ai/llm/career/recommendation/', {
            'self_introduction': '我精通Python编程，熟悉Django框架，有2年后端开发经验。'
        }, content_type='application/json')
        
        # 设置用户（可选）
        request.user = None
        
        print(f"📝 测试自我介绍: {request.body.decode()[:100]}...")
        
        # 调用API
        response = optimized_career_recommendation(request)
        
        print(f"\n📊 API响应:")
        print(f"   状态码: {response.status_code}")
        
        if response.status_code == 200:
            data = json.loads(response.content)
            print(f"   成功: {data.get('success')}")
            print(f"   推荐数量: {len(data.get('recommendations', []))}")
            print(f"   来源: {data.get('source')}")
            print(f"   响应时间: {data.get('response_time', 0):.2f}s")
            
            if data.get('recommendations'):
                print("\n   推荐结果:")
                for i, rec in enumerate(data['recommendations'], 1):
                    print(f"   {i}. {rec.get('career')} (匹配度: {rec.get('matchScore')}%)")
                    print(f"      理由: {rec.get('reason')[:100]}...")
        
        return response
        
    except Exception as e:
        print(f"❌ 完整流程测试失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return None

if __name__ == "__main__":
    print("="*70)
    print("职业推荐代理工具调用调试脚本")
    print("="*70)
    
    # 运行所有测试
    test_agent_creation()
    test_tool_registration()
    test_tool_direct_call()
    test_agent_with_tools()
    test_graph_workflow()
    test_full_recommendation_flow()
    
    print("\n" + "="*70)
    print("调试完成!")
    print("="*70)