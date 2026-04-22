#!/usr/bin/env python3
"""
完整职业推荐流程调试脚本

模拟前端点击"下一步：开始测评"后的完整流程：
1. 用户提交自我介绍
2. 调用推荐代理
3. 代理调用工具（关键词搜索/语义搜索）
4. 返回推荐结果
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

def simulate_frontend_request(self_introduction):
    """模拟前端请求"""
    print("\n" + "="*70)
    print("1. 模拟前端请求")
    print("="*70)
    print(f"📝 用户自我介绍: {self_introduction[:50]}...")
    
    return {
        'self_introduction': self_introduction,
        'session_id': os.urandom(16).hex()
    }

def call_recommendation_agent(request_data):
    """调用推荐代理"""
    print("\n" + "="*70)
    print("2. 调用推荐代理")
    print("="*70)
    
    try:
        from ai_integration.langgraph.agents.recommendation_agent import create_recommendation_agent
        from langchain_core.messages import HumanMessage
        
        # 创建代理
        agent = create_recommendation_agent()
        print("✅ 推荐代理创建成功")
        
        # 构建消息
        messages = [
            HumanMessage(content=f"请根据我的自我介绍推荐合适的职业：{request_data['self_introduction']}")
        ]
        
        # 调用代理
        print("🔄 执行代理推理...")
        result = agent.invoke({"messages": messages})
        
        print(f"\n📊 代理返回类型: {type(result).__name__}")
        
        # 检查是否有工具调用
        if hasattr(result, "tool_calls") and result.tool_calls:
            print(f"✅ 检测到工具调用! 数量: {len(result.tool_calls)}")
            
            tool_calls = []
            for i, tool_call in enumerate(result.tool_calls):
                print(f"\n   工具调用 {i+1}:")
                if isinstance(tool_call, dict):
                    tool_name = tool_call.get('name')
                    tool_args = tool_call.get('args', {})
                else:
                    tool_name = tool_call.name
                    tool_args = tool_call.args
                
                print(f"      名称: {tool_name}")
                print(f"      参数: {json.dumps(tool_args, indent=4, ensure_ascii=False)}")
                
                tool_calls.append({
                    'name': tool_name,
                    'args': tool_args
                })
            
            return {
                'status': 'tool_call',
                'tool_calls': tool_calls
            }
        else:
            print(f"❌ 未检测到工具调用")
            print(f"   直接响应: {result.content[:100]}...")
            return {
                'status': 'direct_response',
                'content': result.content if hasattr(result, 'content') else str(result)
            }
            
    except Exception as e:
        print(f"❌ 代理调用失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return {
            'status': 'error',
            'error': str(e)
        }

def execute_tool_calls(tool_calls):
    """执行工具调用"""
    print("\n" + "="*70)
    print("3. 执行工具调用")
    print("="*70)
    
    from ai_integration.langgraph.tools import (
        keyword_search_tool,
        semantic_search_tool,
        read_chunk_tool
    )
    
    tool_results = []
    
    for tool_call in tool_calls:
        tool_name = tool_call['name']
        tool_args = tool_call['args']
        
        print(f"\n🔧 执行工具: {tool_name}")
        print(f"   参数: {json.dumps(tool_args, ensure_ascii=False)}")
        
        try:
            if tool_name == 'keyword_search_tool':
                result = keyword_search_tool.invoke(tool_args)
            elif tool_name == 'semantic_search_tool':
                result = semantic_search_tool.invoke(tool_args)
            elif tool_name == 'read_chunk_tool':
                result = read_chunk_tool.invoke(tool_args)
            else:
                result = {"success": False, "error": f"未知工具: {tool_name}"}
            
            print(f"✅ 工具执行成功")
            print(f"   结果数量: {result.get('count', 'N/A')}")
            
            tool_results.append({
                'tool_name': tool_name,
                'success': result.get('success', False),
                'results': result.get('results', [])
            })
            
        except Exception as e:
            print(f"❌ 工具执行失败: {str(e)}")
            tool_results.append({
                'tool_name': tool_name,
                'success': False,
                'error': str(e)
            })
    
    return tool_results

def summarize_results(tool_results):
    """汇总工具执行结果"""
    print("\n" + "="*70)
    print("4. 汇总推荐结果")
    print("="*70)
    
    all_recommendations = []
    
    for tool_result in tool_results:
        if tool_result['success'] and tool_result['results']:
            for result in tool_result['results']:
                recommendations = result.get('recommendations', [])
                for rec in recommendations:
                    career = rec.get('career', rec.get('职业'))
                    if career and career not in [r['career'] for r in all_recommendations]:
                        all_recommendations.append({
                            'career': career,
                            'matchScore': rec.get('matchScore', rec.get('匹配度', 0)),
                            'reason': rec.get('reason', rec.get('推荐理由', '')),
                            'source': tool_result['tool_name']
                        })
    
    # 按匹配度排序
    all_recommendations.sort(key=lambda x: x['matchScore'], reverse=True)
    
    # 取前3个
    final_recommendations = all_recommendations[:3]
    
    print(f"\n🎯 最终推荐职业 (共 {len(final_recommendations)} 个):")
    for i, rec in enumerate(final_recommendations, 1):
        print(f"\n   {i}. {rec['career']}")
        print(f"      匹配度: {rec['matchScore']}%")
        print(f"      理由: {rec['reason'][:50]}...")
        print(f"      来源: {rec['source']}")
    
    return final_recommendations

def full_recommendation_flow(self_introduction):
    """完整的职业推荐流程"""
    print("="*70)
    print("完整职业推荐流程调试")
    print("="*70)
    
    # 步骤1: 模拟前端请求
    request_data = simulate_frontend_request(self_introduction)
    
    # 步骤2: 调用推荐代理
    agent_result = call_recommendation_agent(request_data)
    
    if agent_result['status'] == 'error':
        print(f"\n❌ 流程终止: {agent_result['error']}")
        return None
    
    # 步骤3: 如果有工具调用，执行工具
    if agent_result['status'] == 'tool_call':
        tool_results = execute_tool_calls(agent_result['tool_calls'])
        
        # 步骤4: 汇总结果
        final_recommendations = summarize_results(tool_results)
        
        return {
            'success': True,
            'recommendations': final_recommendations,
            'session_id': request_data['session_id']
        }
    else:
        # 直接响应
        return {
            'success': True,
            'direct_response': agent_result['content'],
            'session_id': request_data['session_id']
        }

if __name__ == "__main__":
    # 测试案例
    test_introductions = [
        "我精通Python编程，熟悉Django框架，有2年后端开发经验，参与过多个大型项目。",
        "我对网络安全很感兴趣，熟悉渗透测试和漏洞分析，希望从事安全相关工作。",
        "我擅长数据分析，熟悉Python、SQL和数据可视化工具，有数据挖掘项目经验。"
    ]
    
    for i, intro in enumerate(test_introductions, 1):
        print(f"\n{'='*70}")
        print(f"测试案例 {i}:")
        print(f"{'='*70}")
        
        result = full_recommendation_flow(intro)
        
        if result and result.get('success'):
            print(f"\n✅ 流程执行成功!")
            if 'recommendations' in result:
                print(f"   推荐数量: {len(result['recommendations'])}")
        else:
            print(f"\n❌ 流程执行失败")
    
    print("\n" + "="*70)
    print("所有测试完成!")
    print("="*70)