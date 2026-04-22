#!/usr/bin/env python3
"""
查看所有模式的提示词
"""

import os
import sys
from dotenv import load_dotenv
import django

# 加载环境变量
load_dotenv()

# 设置Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

# 添加项目根目录到Python路径
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from ai_integration.langgraph.utils.prompt_manager import initialize_prompts, get_prompt
from ai_integration.models import AIPrompt

def view_all_prompts():
    """查看所有模式的提示词"""
    print("\n=== 初始化并查看所有提示词 ===")
    
    # 初始化提示词
    print("初始化提示词数据库...")
    initialize_prompts()
    
    # 查看数据库中的提示词
    print("\n数据库中的提示词:")
    prompts = AIPrompt.objects.all()
    for prompt in prompts:
        print(f"\n{prompt.get_prompt_type_display()} ({prompt.prompt_type}):")
        print(f"激活状态: {'是' if prompt.is_active else '否'}")
        print(f"创建时间: {prompt.created_at}")
        print(f"提示词内容:\n{prompt.system_prompt}")
        print("-" * 80)
    
    # 测试获取提示词
    print("\n测试从数据库获取提示词:")
    prompt_types = ['chat', 'interview', 'career', 'recommendation']
    for prompt_type in prompt_types:
        prompt = get_prompt(prompt_type)
        print(f"\n{prompt_type}:\n{prompt}")
        print("-" * 80)

if __name__ == "__main__":
    view_all_prompts()
