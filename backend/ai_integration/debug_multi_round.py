#!/usr/bin/env python3
"""
支持多轮工具调用的调试版本
模拟完整的工具调用循环
"""

import os
import sys
import json
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[logging.StreamHandler()]
)

logger = logging.getLogger('debug_recommendation')

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from langchain_core.messages import HumanMessage, AIMessage, ToolMessage
from ai_integration.langgraph.agents.recommendation_agent import create_recommendation_agent
from ai_integration.langgraph.tools import keyword_search_tool, semantic_search_tool, read_chunk_tool

def execute_tool(tool_call):
    """执行单个工具调用"""
    tool_name = tool_call.get('name')
    tool_args = tool_call.get('args', {})
    tool_id = tool_call.get('id', 'unknown')
    
    print(f"      🔧 执行工具: {tool_name}")
    print(f"      参数: {json.dumps(tool_args, ensure_ascii=False)}")
    
    try:
        if tool_name == 'keyword_search_tool':
            result = keyword_search_tool.invoke(tool_args)
        elif tool_name == 'semantic_search_tool':
            result = semantic_search_tool.invoke(tool_args)
        elif tool_name == 'read_chunk_tool':
            result = read_chunk_tool.invoke(tool_args)
        else:
            result = {"success": False, "error": f"未知工具: {tool_name}"}
        
        print(f"      ✅ 工具执行成功")
        print(f"      结果数量: {result.get('count', 'N/A')}")
        
        return {
            'tool_call_id': tool_id,
            'output': result,
            'success': result.get('success', False)
        }
        
    except Exception as e:
        print(f"      ❌ 工具执行失败: {str(e)}")
        return {
            'tool_call_id': tool_id,
            'output': f"工具执行失败: {str(e)}",
            'success': False
        }

def debug_recommendation_multi_round(self_introduction):
    """
    支持多轮工具调用的调试版本
    """
    print("\n" + "="*80)
    print("🚀 开始职业推荐流程（多轮工具调用）")
    print("="*80)
    
    round_count = 0
    max_rounds = 5
    
    # 初始化代理
    print("\n📌 Step 1: 初始化推荐代理")
    print("-" * 50)
    try:
        agent = create_recommendation_agent()
        print("✅ 推荐代理创建成功")
    except Exception as e:
        print(f"❌ 代理创建失败: {str(e)}")
        return None
    
    # 构建初始消息
    messages = [HumanMessage(content=self_introduction)]
    print(f"\n📌 Step 2: 用户输入")
    print("-" * 50)
    print(f"   用户自我介绍: {self_introduction[:50]}...")
    
    while round_count < max_rounds:
        round_count += 1
        
        print(f"\n" + "="*60)
        print(f"🔄 第 {round_count} 轮调用")
        print("="*60)
        
        # 调用代理
        print(f"\n📌 Step {round_count*2}: 调用代理")
        print("-" * 50)
        print(f"   消息数量: {len(messages)}")
        print("   正在调用代理...")
        
        try:
            result = agent.invoke({"messages": messages})
            print(f"✅ 代理调用成功")
            print(f"   返回类型: {type(result).__name__}")
            
            # 检查工具调用
            if hasattr(result, 'tool_calls') and result.tool_calls:
                print(f"\n🎉 检测到工具调用!")
                
                tool_calls = []
                for i, tc in enumerate(result.tool_calls):
                    if isinstance(tc, dict):
                        tool_calls.append({
                            'name': tc.get('name'),
                            'args': tc.get('args', {}),
                            'id': tc.get('id', f'call_{i}')
                        })
                    else:
                        tool_calls.append({
                            'name': tc.name,
                            'args': tc.args,
                            'id': tc.id
                        })
                
                # 执行工具调用
                print(f"\n📌 Step {round_count*2 + 1}: 执行工具调用")
                print("-" * 50)
                
                tool_results = []
                for tc in tool_calls:
                    result = execute_tool(tc)
                    tool_results.append(result)
                
                # 添加工具结果消息
                for tr in tool_results:
                    messages.append(ToolMessage(
                        content=json.dumps(tr['output'], ensure_ascii=False),
                        tool_call_id=tr['tool_call_id']
                    ))
                
                # 添加代理的工具调用消息
                messages.append(result)
                
            else:
                # 没有工具调用，说明是最终回答
                print(f"\n🎉 获得最终回答!")
                print(f"\n💬 回答内容:")
                print("="*50)
                if hasattr(result, 'content') and result.content:
                    print(result.content)
                    
                    # 提取JSON
                    content = result.content
                    start = content.find('```json')
                    end = content.find('```', start + 7)
                    
                    if start != -1 and end != -1:
                        try:
                            json_str = content[start + 7:end].strip()
                            recommendations = json.loads(json_str)
                            print(f"\n📊 职业推荐结果:")
                            for rec in recommendations.get('recommendations', []):
                                print(f"   - {rec.get('career')}: {rec.get('matchScore')}%")
                        except:
                            print("\n⚠️ 无法解析JSON结果")
                else:
                    print("❌ 回答内容为空")
                
                print("="*50)
                print("\n✅ 流程完成!")
                return result
                
        except Exception as e:
            print(f"❌ 代理调用失败: {str(e)}")
            import traceback
            traceback.print_exc()
            return None
    
    print(f"\n⚠️ 达到最大轮数 {max_rounds}，流程终止")
    return None

if __name__ == "__main__":
    test_introduction = "我精通Python编程，熟悉Django框架，有2年后端开发经验，参与过多个大型项目。希望找到适合我的职业发展方向。"
    debug_recommendation_multi_round(test_introduction)