#!/usr/bin/env python3
"""添加网络安全相关测试数据"""

import os
import sys
import json

sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'backend'))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.arag_integration import ARAGCareerSearch

def add_cyber_security_data():
    print("添加网络工程测试数据...")
    
    search = ARAGCareerSearch()
    
    # 添加网络工程相关记录
    cyber_security_record = {
        "id": "test_cyber",
        "user_id": "99",
        "self_introduction": "我对网络工程有浓厚兴趣，熟悉网络协议、配置和维护，了解TCP/IP协议栈，熟悉常用网络设备（如交换机、路由器）的配置。",
        "introduction_preview": "对网络工程有浓厚兴趣，熟悉网络协议、配置和维护，了解TCP/IP协议栈，熟悉常用网络设备（如交换机、路由器）的配置",     
        "recommendations": json.dumps([
            {"career": "网络工程师", "matchScore": 95, "reason": "网络工程技能匹配度高"},
            {"career": "渗透测试工程师", "matchScore": 90, "reason": "熟悉渗透测试工具和方法"},
            {"career": "安全运维工程师", "matchScore": 85, "reason": "具备安全防护基础知识"}
        ]),
        "analysis_result": "分析结果：网络工程方向",
        "created_at": "2024-01-15T00:00:00"
    }
    
    search.add_record(cyber_security_record)
    print("✅ 网络工程测试数据添加成功")
    
    # 验证添加结果
    result = search.keyword_search("网络工程", top_k=1)
    if result:
        print(f"✅ 验证成功：找到网络工程相关记录")
        print(f"   记录预览: {result[0].get('introduction_preview', '')}")
    else:
        print("❌ 验证失败：未找到网络工程相关记录")        

if __name__ == '__main__':
    add_cyber_security_data()