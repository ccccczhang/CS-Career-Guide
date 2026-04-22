#!/usr/bin/env python3
"""
职业推荐调试入口

运行此文件来测试职业推荐流程，查看详细的调试日志
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.debug_multi_round import debug_recommendation_multi_round

def main():
    print("="*80)
    print("🎯 职业推荐调试工具")
    print("="*80)
    
    # 用户自我介绍（模拟前端输入）
    self_introduction = input("请输入你的自我介绍（按回车使用默认内容）：\n")
    
    if not self_introduction.strip():
        # 默认测试内容
        self_introduction = """我精通Python编程，熟悉Django框架，有2年后端开发经验，参与过多个大型项目。
掌握Java、JavaScript等编程语言，熟悉MySQL、PostgreSQL数据库。
对机器学习和人工智能有浓厚兴趣，希望找到适合我的职业发展方向。"""
    
    print("\n" + "="*80)
    print("开始执行职业推荐流程...")
    print("="*80)
    
    # 执行调试流程
    debug_recommendation_multi_round(self_introduction)

if __name__ == "__main__":
    main()