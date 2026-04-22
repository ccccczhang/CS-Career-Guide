#!/usr/bin/env python3
"""
调试版本的职业推荐视图
在关键步骤输出详细日志，帮助排查问题
"""

import os
import sys
import json
import logging

# 设置详细日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler()
    ]
)

logger = logging.getLogger('debug_recommendation')

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from langchain_core.messages import HumanMessage, AIMessage
from ai_integration.langgraph.agents.recommendation_agent import create_recommendation_agent
from ai_integration.langgraph.tools import keyword_search_tool, semantic_search_tool, read_chunk_tool

def debug_recommendation_workflow(self_introduction):
    """
    带详细调试日志的职业推荐流程
    
    Args:
        self_introduction: 用户自我介绍内容
    """
    print("\n" + "="*80)
    print("🚀 开始职业推荐流程")
    print("="*80)
    
    step = 1
    
    # Step 1: 初始化代理
    print(f"\n📌 Step {step}: 初始化推荐代理")
    print("-" * 50)
    try:
        agent = create_recommendation_agent()
        print("✅ 推荐代理创建成功")
        print(f"   代理类型: {type(agent).__name__}")
    except Exception as e:
        print(f"❌ 代理创建失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return None
    step += 1
    
    # Step 2: 构建用户消息
    print(f"\n📌 Step {step}: 构建用户消息")
    print("-" * 50)
    print(f"   用户自我介绍: {self_introduction[:50]}...")
    messages = [HumanMessage(content=self_introduction)]
    print("✅ 消息构建完成")
    step += 1
    
    # Step 3: 第一次调用代理（期望工具调用）
    print(f"\n📌 Step {step}: 第一次调用代理（期望工具调用）")
    print("-" * 50)
    print("   正在调用代理...")
    
    try:
        result = agent.invoke({"messages": messages})
        print(f"✅ 代理调用成功")
        print(f"   返回类型: {type(result).__name__}")
        
        # 检查工具调用
        if hasattr(result, 'tool_calls') and result.tool_calls:
            print(f"🎉 检测到工具调用!")
            
            tool_calls = []
            for i, tool_call in enumerate(result.tool_calls):
                if isinstance(tool_call, dict):
                    tool_name = tool_call.get('name')
                    tool_args = tool_call.get('args', {})
                else:
                    tool_name = tool_call.name
                    tool_args = tool_call.args
                
                print(f"\n   工具调用 {i+1}:")
                print(f"      名称: {tool_name}")
                print(f"      参数: {json.dumps(tool_args, indent=4, ensure_ascii=False)}")
                tool_calls.append({'name': tool_name, 'args': tool_args})
            
        else:
            print(f"⚠️ 未检测到工具调用")
            if hasattr(result, 'content'):
                print(f"   响应内容预览: {result.content[:100]}...")
            return None
            
    except Exception as e:
        print(f"❌ 代理调用失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return None
    step += 1
    
    # Step 4: 执行工具调用
    print(f"\n📌 Step {step}: 执行工具调用")
    print("-" * 50)
    
    tool_results = []
    for tool_call in tool_calls:
        tool_name = tool_call['name']
        tool_args = tool_call['args']
        
        print(f"\n   🔧 执行工具: {tool_name}")
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
            
            if result.get('results'):
                for r in result['results'][:2]:
                    preview = r.get('preview', '')[:30]
                    score = r.get('match_score', 0)
                    print(f"         - {preview}... (匹配度: {score:.4f})")
            
            tool_results.append({
                'tool_name': tool_name,
                'success': result.get('success', False),
                'count': result.get('count', 0)
            })
            
        except Exception as e:
            print(f"      ❌ 工具执行失败: {str(e)}")
            tool_results.append({
                'tool_name': tool_name,
                'success': False,
                'error': str(e)
            })
    step += 1
    
    # Step 5: 第二次调用代理（期望最终回答）
    print(f"\n📌 Step {step}: 第二次调用代理（期望最终回答）")
    print("-" * 50)
    print("   正在调用代理...")
    
    try:
        # 将工具结果添加到消息
        tool_result_content = f"工具执行结果:\n{json.dumps(tool_results, ensure_ascii=False)}"
        tool_message = AIMessage(content=tool_result_content)
        messages.append(tool_message)
        
        final_result = agent.invoke({"messages": messages})
        
        print(f"✅ 代理调用成功")
        
        if hasattr(final_result, 'content') and final_result.content:
            print(f"\n   🎉 成功获得最终回答!")
            print(f"\n   💬 回答内容:")
            print("   " + "="*50)
            print(final_result.content)
            print("   " + "="*50)
            
            # 提取JSON结果
            content = final_result.content
            start = content.find('```json')
            end = content.find('```', start + 7)
            
            if start != -1 and end != -1:
                try:
                    json_str = content[start + 7:end].strip()
                    recommendations = json.loads(json_str)
                    
                    print(f"\n   📊 解析出的职业推荐:")
                    for rec in recommendations.get('recommendations', []):
                        print(f"      - {rec.get('career')}: {rec.get('matchScore')}%")
                        
                except json.JSONDecodeError as e:
                    print(f"   ⚠️ JSON解析失败: {str(e)}")
        else:
            print(f"   ❌ 未获得有效回答")
            
    except Exception as e:
        print(f"❌ 代理调用失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return None
    step += 1
    
    print("\n" + "="*80)
    print("✅ 职业推荐流程完成")
    print("="*80)
    
    return final_result

if __name__ == "__main__":
    # 测试数据
    test_introduction = "我精通Python编程，熟悉Django框架，有2年后端开发经验，参与过多个大型项目。希望找到适合我的职业发展方向。"
    
    # 执行调试流程
    debug_recommendation_workflow(test_introduction)