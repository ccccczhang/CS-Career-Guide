#!/usr/bin/env python3
"""
完整端到端测试脚本 - 模拟前端点击"下一步：开始测评"
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

def simulate_full_recommendation_flow(self_introduction):
    """模拟完整的职业推荐流程"""
    print("\n" + "="*70)
    print("模拟完整职业推荐流程")
    print("="*70)
    
    from ai_integration.langgraph.agents.recommendation_agent import create_recommendation_agent
    from ai_integration.langgraph.tools import keyword_search_tool, semantic_search_tool, read_chunk_tool
    from langchain_core.messages import HumanMessage, AIMessage
    
    # 创建代理
    agent = create_recommendation_agent()
    print("✅ 推荐代理创建成功")
    
    # 步骤1: 用户提交自我介绍
    print(f"\n📝 用户自我介绍: {self_introduction[:50]}...")
    
    # 步骤2: 第一次调用代理（期望工具调用）
    messages = [HumanMessage(content=self_introduction)]
    print("\n🔄 第一次调用代理（期望工具调用）...")
    
    result = agent.invoke({"messages": messages})
    print(f"📊 返回类型: {type(result).__name__}")
    
    if hasattr(result, 'tool_calls') and result.tool_calls:
        print(f"✅ 成功检测到工具调用!")
        
        # 提取工具调用
        tool_calls = []
        for tool_call in result.tool_calls:
            if isinstance(tool_call, dict):
                tool_calls.append({
                    'name': tool_call.get('name'),
                    'args': tool_call.get('args', {}),
                    'id': tool_call.get('id')
                })
            else:
                tool_calls.append({
                    'name': tool_call.name,
                    'args': tool_call.args,
                    'id': tool_call.id
                })
        
        # 步骤3: 执行工具调用
        print("\n🔧 执行工具调用:")
        tool_results = []
        for tool_call in tool_calls:
            tool_name = tool_call['name']
            tool_args = tool_call['args']
            
            print(f"\n   执行工具: {tool_name}")
            print(f"   参数: {json.dumps(tool_args, ensure_ascii=False)}")
            
            try:
                if tool_name == 'keyword_search_tool':
                    result = keyword_search_tool.invoke(tool_args)
                elif tool_name == 'semantic_search_tool':
                    result = semantic_search_tool.invoke(tool_args)
                elif tool_name == 'read_chunk_tool':
                    result = read_chunk_tool.invoke(tool_args)
                
                print(f"   ✅ 工具执行成功")
                print(f"   结果数量: {result.get('count', 0)}")
                
                if result.get('results'):
                    for r in result['results'][:2]:
                        print(f"     - {r.get('introduction_preview', '')[:30]}...")
                
                tool_results.append({
                    'tool_name': tool_name,
                    'args': tool_args,
                    'result': result
                })
                
            except Exception as e:
                print(f"   ❌ 工具执行失败: {str(e)}")
                tool_results.append({
                    'tool_name': tool_name,
                    'args': tool_args,
                    'error': str(e)
                })
        
        # 步骤4: 第二次调用代理（期望最终回答）
        print("\n🔄 第二次调用代理（期望最终回答）...")
        
        # 将工具结果添加到消息历史
        tool_result_message = AIMessage(
            content=f"工具执行结果:\n{json.dumps(tool_results, ensure_ascii=False, indent=2)}",
            tool_calls=tool_calls,
            tool_results=tool_results
        )
        
        messages.append(tool_result_message)
        
        final_result = agent.invoke({"messages": messages})
        
        if hasattr(final_result, 'content'):
            print(f"✅ 成功获得最终回答!")
            print(f"\n💬 回答内容:\n{final_result.content}")
            
            # 提取JSON结果
            content = final_result.content
            start = content.find('```json')
            end = content.find('```', start + 7)
            
            if start != -1 and end != -1:
                json_str = content[start + 7:end].strip()
                try:
                    recommendations = json.loads(json_str)
                    print(f"\n📊 解析出的职业推荐:")
                    for rec in recommendations.get('recommendations', []):
                        print(f"   - {rec.get('career')}: {rec.get('matchScore')}%")
                except:
                    print("\n⚠️ 无法解析JSON结果")
        
        else:
            print(f"❌ 未获得最终回答")
            
    else:
        print(f"❌ 未检测到工具调用")
        if hasattr(result, 'content'):
            print(f"   响应内容: {result.content[:200]}...")

if __name__ == "__main__":
    print("="*70)
    print("端到端测试 - 模拟前端'下一步：开始测评'")
    print("="*70)
    
    # 测试案例
    test_introduction = "我精通Python编程，熟悉Django框架，有2年后端开发经验，参与过多个大型项目。希望找到适合我的职业发展方向。"
    
    simulate_full_recommendation_flow(test_introduction)
    
    print("\n" + "="*70)
    print("测试完成!")
    print("="*70)