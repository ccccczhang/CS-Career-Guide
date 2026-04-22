#!/usr/bin/env python3
"""
测试完整的 LangGraph 工作流
验证推荐代理调用工具的完整流程
"""

import os
import sys
import json
import time
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[logging.StreamHandler()]
)

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from langchain_core.messages import HumanMessage
from ai_integration.langgraph.graphs.graph import create_chat_workflow

def test_langgraph_recommendation():
    """测试 LangGraph 推荐工作流"""
    print("\n" + "="*80)
    print("🚀 测试 LangGraph 职业推荐工作流")
    print("="*80)
    
    start_time = time.time()
    
    # 创建工作流
    print("\n📌 Step 1: 创建工作流")
    print("-" * 50)
    try:
        workflow = create_chat_workflow()
        print("✅ 工作流创建成功")
    except Exception as e:
        print(f"❌ 工作流创建失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return
    
    # 准备输入
    print("\n📌 Step 2: 准备输入")
    print("-" * 50)
    self_introduction = """我精通Python编程，熟悉Django框架，有2年后端开发经验。
掌握Java、JavaScript等编程语言，熟悉MySQL、PostgreSQL数据库。
对机器学习和人工智能有浓厚兴趣。"""
    
    print(f"用户自我介绍: {self_introduction[:50]}...")
    
    state = {
        "messages": [HumanMessage(content=self_introduction)],
        "mode": "recommendation",
        "response": "",
        "tool_calls": None,
        "tool_results": None,
        "last_agent": None
    }
    print("✅ 输入准备完成")
    
    # 执行工作流
    print("\n📌 Step 3: 执行工作流")
    print("-" * 50)
    print("正在执行...")
    
    try:
        result = workflow.invoke(state)
        print("✅ 工作流执行成功")
        
        # 分析结果
        print("\n📌 Step 4: 分析结果")
        print("-" * 50)
        
        print(f"结果键: {list(result.keys())}")
        
        if "response" in result:
            response = result["response"]
            print(f"响应内容长度: {len(response)}")
            print(f"\n💬 响应内容:")
            print("="*50)
            print(response[:500] + "..." if len(response) > 500 else response)
            print("="*50)
            
            # 尝试提取JSON
            import re
            json_match = re.search(r'```json\s*([\s\S]*?)\s*```', response)
            if json_match:
                try:
                    json_str = json_match.group(1).strip()
                    data = json.loads(json_str, strict=False)
                    print("\n📊 提取的JSON推荐:")
                    if 'recommendations' in data:
                        for rec in data['recommendations'][:3]:
                            print(f"   - {rec.get('career')}: {rec.get('matchScore')}%")
                except:
                    print("⚠️ JSON解析失败")
        
        if "tool_calls" in result and result["tool_calls"]:
            print(f"\n⚠️ 仍有未处理的工具调用: {result['tool_calls']}")
        
    except Exception as e:
        print(f"❌ 工作流执行失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return
    
    total_time = time.time() - start_time
    print(f"\n" + "="*80)
    print(f"✅ 测试完成，总耗时: {total_time:.2f}秒")
    print("="*80)

if __name__ == "__main__":
    test_langgraph_recommendation()