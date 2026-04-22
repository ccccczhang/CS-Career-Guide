#!/usr/bin/env python3
"""
测试职业推荐API
模拟前端点击"开始测评"后的请求
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

from ai_integration.langgraph_views import career_recommendation
from django.test import RequestFactory

def test_career_recommendation_api():
    """测试职业推荐API"""
    print("="*80)
    print("测试职业推荐API")
    print("="*80)
    
    # 创建模拟请求
    factory = RequestFactory()
    
    # 用户自我介绍（模拟前端输入）
    self_introduction = """我精通Python编程，熟悉Django框架，有2年后端开发经验，参与过多个大型项目。
掌握Java、JavaScript等编程语言，熟悉MySQL、PostgreSQL数据库。
对机器学习和人工智能有浓厚兴趣，希望找到适合我的职业发展方向。"""
    
    # 创建POST请求
    request_data = json.dumps({
        'self_introduction': self_introduction
    })
    
    request = factory.post('/api/career/recommendation/', data=request_data, content_type='application/json')
    
    # 设置认证状态（模拟未认证用户）
    from django.contrib.auth.models import AnonymousUser
    request.user = AnonymousUser()
    
    print(f"\n📝 用户自我介绍: {self_introduction[:50]}...")
    print("\n🔄 调用职业推荐API...")
    
    # 调用视图函数
    response = career_recommendation(request)
    
    print(f"\n📊 响应类型: {type(response).__name__}")
    print(f"📋 响应状态: {response.status_code}")
    
    # 解析响应内容
    if response.status_code == 200:
        response_content = response.content.decode('utf-8')
        try:
            data = json.loads(response_content)
            print(f"\n✅ API调用成功!")
            print(f"   成功: {data.get('success')}")
            print(f"   来源: {data.get('source')}")
            
            if data.get('recommendations'):
                print(f"\n🎯 职业推荐结果:")
                for i, rec in enumerate(data['recommendations'], 1):
                    print(f"   {i}. {rec.get('career')}: {rec.get('matchScore')}%")
                    if rec.get('reason'):
                        print(f"      理由: {rec['reason'][:30]}...")
            else:
                print(f"\n⚠️ 没有推荐结果")
                
        except json.JSONDecodeError:
            print(f"\n❌ 无法解析响应为JSON")
            print(f"   响应内容: {response_content[:500]}...")
    else:
        print(f"\n❌ API调用失败")
        print(f"   错误: {response.content.decode('utf-8')}")

if __name__ == "__main__":
    test_career_recommendation_api()