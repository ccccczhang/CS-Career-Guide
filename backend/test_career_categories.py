#!/usr/bin/env python3
"""
测试职业分类信息是否正确加载到推荐代理的提示词中
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.langgraph.utils.prompt_manager import get_career_categories, get_prompt
from ai_integration.langgraph.agents.recommendation_agent import create_recommendation_agent

def test_career_categories():
    """测试职业分类信息"""
    print("\n" + "="*80)
    print("🚀 测试职业分类信息加载")
    print("="*80)
    
    # 1. 获取职业分类信息
    print("\n📌 Step 1: 获取职业分类信息")
    print("-" * 50)
    career_info = get_career_categories()
    
    if career_info:
        print("✅ 成功获取职业分类信息")
        print(f"内容长度: {len(career_info)} 字符")
        print("\n📋 职业分类内容预览:")
        print("-" * 50)
        print(career_info[:1000] + "..." if len(career_info) > 1000 else career_info)
    else:
        print("❌ 未获取到职业分类信息")
        print("请检查：")
        print("  1. career_evaluation 应用是否已安装")
        print("  2. 数据库中是否有 CareerCategory 和 CareerSubcategory 数据")
        print("  3. 运行 python manage.py migrate")
    
    # 2. 获取推荐提示词
    print("\n" + "="*80)
    print("📌 Step 2: 获取推荐提示词")
    print("-" * 50)
    prompt = get_prompt('recommendation')
    
    if prompt:
        print("✅ 成功获取推荐提示词")
        print(f"提示词长度: {len(prompt)} 字符")
        
        # 检查是否包含职业分类信息
        if "职业九个大类" in prompt:
            print("✅ 提示词中包含职业分类信息")
        else:
            print("❌ 提示词中未包含职业分类信息")
            
        print("\n📋 提示词内容预览（前500字符）:")
        print("-" * 50)
        print(prompt[:500] + "..." if len(prompt) > 500 else prompt)
    else:
        print("❌ 未获取到推荐提示词")
    
    # 3. 测试推荐代理创建
    print("\n" + "="*80)
    print("📌 Step 3: 测试推荐代理创建")
    print("-" * 50)
    try:
        agent = create_recommendation_agent()
        print("✅ 推荐代理创建成功")
        
        # 检查工具绑定
        print("\n🔧 已绑定的工具:")
        tools = [keyword_search_tool, semantic_search_tool, read_chunk_tool, get_time]
        for tool in tools:
            print(f"   - {tool.name}: {tool.description[:30]}...")
            
    except Exception as e:
        print(f"❌ 推荐代理创建失败: {str(e)}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    test_career_categories()